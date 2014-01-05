package jGalapagos.worker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;

public final class ObjectUtilities {
	
	/**
	 * Koristi se jer ne radi SerializationUtils.deserialize(objectData);
	 * 
	 * @param <T>
	 * @param byteArray
	 * @return Vraća <code>null</code> ako nije uspješno deserijalizirano
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T toObject(byte[] byteArray) {
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
		ObjectInputStream objectInputStream = null;
		T object = null;
		try {
			objectInputStream = new ObjectInputStream(byteArrayInputStream);
			object = (T) objectInputStream.readObject();
		} catch (Exception e) {
		} finally {
			try { objectInputStream.close(); } catch (Exception e) { }
		}
		return object;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T toObject(ClassLoader classLoader, byte[] byteArray) {
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
		ClassLoaderObjectInputStream objectInputStream = null;
		T object = null;
		try {
			objectInputStream = new ClassLoaderObjectInputStream(classLoader, byteArrayInputStream);
			object = (T) objectInputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { objectInputStream.close(); } catch (Exception e) { }
		}
		return object;
	}
	
	private static class ClassLoaderObjectInputStream extends ObjectInputStream{
		 
		private final ClassLoader classLoader;
	 
		public ClassLoaderObjectInputStream(ClassLoader classLoader, InputStream in) throws IOException {
			super(in);
			this.classLoader = classLoader;
		}
		
		@Override
		protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		
			try{
				String name = desc.getName();
				return Class.forName(name, false, classLoader);
			}
			catch(ClassNotFoundException e){
				return super.resolveClass(desc);
			}
		}
	}

}
