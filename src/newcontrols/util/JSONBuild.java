package newcontrols.util;

import arc.struct.*;

/** Simple string-only json constructor. Performs simple verification that prevents from creating invalid json objects.
 * Unused now... Had a use during private development. */
public class JSONBuild {
	public enum Type { OBJECT, ARRAY };
	
	public StringBuilder b = new StringBuilder();
	protected IntSeq stack = new IntSeq();
	protected Seq<Type> typeStack = new Seq();
	
	public JSONBuild() {
		reset();
	}
	
	/** Finishes the object & resets the builder */
	public String build() {
		if (stack.size > 1) throw new IllegalStateException("Unterminated array or object at the end of json string");
		String result = b.append('}').toString();
		reset();
		return result;
	}
	
	public JSONBuild addField(String key, String value) {
		stackValidate(Type.OBJECT);
		addComma();
		b.append('"').append(key).append("\":\"").append(value).append('"');
		stackNext();
		return this;
	}
	
	/** Used in arrays */
	public JSONBuild beginArray() {
		stackValidate(Type.ARRAY);
		addComma();
		b.append('[');
		stackForward(Type.ARRAY);
		return this;
	}
	
	/** Used in objects */
	public JSONBuild beginArray(String name) {
		stackValidate(Type.OBJECT);
		addComma();
		b.append('"').append(name).append("\":[");
		stackForward(Type.ARRAY);
		return this;
	}
	
	public JSONBuild addValue(String value) {
		stackValidate(Type.ARRAY);
		addComma();
		b.append('"').append(value).append('"');
		stackNext();
		return this;
	}
	
	public JSONBuild endArray() {
		b.append(']');
		stackBackward(Type.ARRAY);
		return this;
	}
	
	/** Used in arrays */
	public JSONBuild beginObject() {
		stackValidate(Type.ARRAY);
		addComma();
		b.append('{');
		stackForward(Type.OBJECT);
		return this;
	}
	
	/** Used in objects */
	public JSONBuild beginObject(String name) {
		stackValidate(Type.OBJECT);
		addComma();
		b.append('"').append(name).append("\":{");
		stackForward(Type.OBJECT);
		return this;
	}
	
	public JSONBuild endObject() {
		b.append('}');
		stackBackward(Type.OBJECT);
		return this;
	}
	
	public JSONBuild reset() {
		b.setLength(0);
		b.append("{");
		stack.clear();
		stack.add(0);
		typeStack.clear();
		typeStack.add(Type.OBJECT);
		return this;
	}
	
	//utility
	protected void stackValidate(Type type) {
		if (lastType() != type) throw new IllegalStateException("Incorrect parent type");
	}
	
	protected void stackForward(Type type) {
		stack.add(0);
		typeStack.add(type);
	}
	
	protected Type lastType() {
		if (typeStack.size < 1) return null;
		return typeStack.peek();
	}
	
	protected void stackBackward(Type type) {
		if (stack.size < 1) return;
		
		if (lastType() != type) throw new IllegalStateException("Incorrect type closed");
		
		stack.removeIndex(stack.size - 1);
		typeStack.remove(typeStack.size - 1);
		stackNext();
	}
	
	protected void addComma() {
		if (stackLast() > 0) b.append(',');
	}
	
	protected void stackNext() {
		if (stack.size < 1) return;
		stack.incr(stack.size - 1, 1);
	}
	
	protected int stackLast() {
		if (stack.size < 1) return 0;
		return stack.peek();
	}
}
