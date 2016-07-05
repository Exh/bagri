package com.bagri.xdm.client.hazelcast.serialize.query;

import java.io.IOException;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.query.AlwaysExpression;
import com.bagri.xdm.query.BinaryExpression;
import com.bagri.xdm.query.Comparison;
import com.bagri.xdm.query.Expression;
import com.bagri.xdm.query.PathBuilder;
import com.bagri.xdm.query.PathExpression;
import com.bagri.xdm.query.QueriedPath;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class ExpressionSerializer implements StreamSerializer<Expression> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_Expression;
	}

	@Override
	public Expression read(ObjectDataInput in) throws IOException {
		int clnId = in.readInt();
		Comparison compType = Comparison.values()[in.readInt()];
		byte type = in.readByte();
		if (type == 0) {
			return new AlwaysExpression(clnId);
		} 
		PathBuilder path = in.readObject();
		if (type == 1) {
			return new BinaryExpression(clnId, compType, path);
		} else {
			return new PathExpression(clnId, compType, path, in.readUTF(), 
					(QueriedPath) in.readObject());
		}
	}

	@Override
	public void write(ObjectDataOutput out, Expression exp) throws IOException {
		out.writeInt(exp.getCollectionId());
		out.writeInt(exp.getCompType().ordinal());
		if (exp instanceof AlwaysExpression) {
			out.writeByte(0);
		} else if (exp instanceof BinaryExpression) {
			out.writeByte(1);
			out.writeObject(exp.getPath());
		} else {
			out.writeByte(2);
			out.writeObject(exp.getPath());
			out.writeUTF(((PathExpression) exp).getParamName());
			out.writeObject(((PathExpression) exp).getCachedPath());
		}
	}

}