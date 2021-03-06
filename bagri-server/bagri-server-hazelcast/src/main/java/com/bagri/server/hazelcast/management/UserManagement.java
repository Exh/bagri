/**
 * 
 */
package com.bagri.server.hazelcast.management;

import static com.bagri.core.server.api.CacheConstants.CN_SYS_ROLES;

import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.core.system.Role;
import com.bagri.core.system.User;
import com.bagri.server.hazelcast.task.user.UserCreator;
import com.bagri.server.hazelcast.task.user.UserRemover;
import com.bagri.support.security.Encryptor;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * @author Denis Sukhoroslov email: dsukhoroslov@gmail.com
 *
 */
@ManagedResource(objectName="com.bagri.db:type=Management,name=UserManagement", 
	description="User Management MBean")
public class UserManagement extends EntityManagement<User> {

	public UserManagement(HazelcastInstance hzInstance) {
		super(hzInstance);
	}

	@ManagedAttribute(description="Current User Name")
	public String getCurrentUser() {
		return super.getCurrentUser();
	}
	
	@ManagedAttribute(description="Registered Users")
	public String[] getUserNames() {
		return getEntityNames();
	}
	
	@ManagedAttribute(description="Return registered Users")
	public TabularData getUsers() {
		return getEntities("user", "User definition");
    }
	
	@ManagedOperation(description="Create new User")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "login", description = "User login"),
		@ManagedOperationParameter(name = "password", description = "User password")})
	public boolean addUser(String login, String password) {
		if (!entityCache.containsKey(login)) {
	    	Object result = entityCache.executeOnKey(login, new UserCreator(getCurrentUser(), password));
	    	logger.debug("addUser; execution result: {}", result);
			return true;
		}
		return false;
	}

	@ManagedOperation(description="Delete User")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "login", description = "User login")})
	public boolean deleteUser(String login) {
		User user = entityCache.get(login);
		if (user != null) {
	    	Object result = entityCache.executeOnKey(login, new UserRemover(user.getVersion(), getCurrentUser()));
	    	logger.debug("deleteUser; execution result: {}", result);
	    	return result != null;
		}
		return false;
	}

	@Override
	protected EntityManager<User> createEntityManager(String userName) {
		UserManager mgr = new UserManager(hzInstance, userName);
		mgr.setEntityCache(entityCache);
		IMap<String, Role> roles = hzInstance.getMap(CN_SYS_ROLES);
		mgr.setRoleCache(roles);
		return mgr;
	}
	
	public boolean authenticate(String login, String password) {
		User user = entityCache.get(login);
		if (user != null) {
			String pwd = Encryptor.encrypt(password);
			if (pwd.equals(user.getPassword())) {
				return true;
			}
			// is password already encrypted?
			if (password.equals(user.getPassword())) {
				return true;
			}
			// fallback to double-encrypted pwd?
			pwd = Encryptor.encrypt(user.getPassword());
			return password.equals(pwd);
		}
		// throw NotFound exception?
		return false;
	}

	public String getUserPassword(String login) {
		User user = entityCache.get(login);
		if (user != null) {
			return user.getPassword();
		}
		return null;
	}

}
