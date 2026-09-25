package org.apache.sshd.client;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.function.FailableBiFunction;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.session.ClientSessionCreator;
import org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.future.VerifiableFuture;
import org.apache.sshd.common.session.SessionHolder;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellCommandFactory;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.reflect.Reflection;

import io.github.toolfactory.narcissus.Narcissus;

public class SftpUploadBatchTest {

	private static Method METHOD_GET_NAME, METHOD_IS_SUCCESS, METHOD_COLLECT, METHOD_EXISTS, METHOD_IS_FILE,
			METHOD_TO_PATH = null;

	@BeforeClass
	static void beforeClass() throws Throwable {
		//
		final Class<?> clz = SftpUploadBatch.class;
		//
		(METHOD_GET_NAME = clz.getDeclaredMethod("getName", Member.class)).setAccessible(true);
		//
		(METHOD_IS_SUCCESS = clz.getDeclaredMethod("isSuccess", AuthFuture.class)).setAccessible(true);
		//
		(METHOD_COLLECT = clz.getDeclaredMethod("collect", Stream.class, Collector.class)).setAccessible(true);
		//
		(METHOD_EXISTS = clz.getDeclaredMethod("exists", File.class)).setAccessible(true);
		//
		(METHOD_IS_FILE = clz.getDeclaredMethod("isFile", File.class)).setAccessible(true);
		//
		(METHOD_TO_PATH = clz.getDeclaredMethod("toPath", File.class)).setAccessible(true);
		//
	}

	private static class IH implements InvocationHandler {

		private Boolean test, isSuccess;

		private Integer size;

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			//
			final String name = getName(method);
			//
			if (Objects.equals(method != null ? method.getReturnType() : null, Void.TYPE)) {
				//
				return null;
				//
			} // if
				//
			if (proxy instanceof Collection) {
				//
				if (Objects.equals(name, "size")) {
					//
					return size;
					//
				} else if (Objects.equals(name, "stream")) {
					//
					return null;
					//
				} // if
					//
			} // if
				//
			if (Boolean.logicalAnd(proxy instanceof Map, contains(Arrays.asList("get", "put"), name))) {
				//
				return null;
				//
			} else if (Boolean.logicalOr(proxy instanceof Predicate, proxy instanceof BiPredicate)
					&& Objects.equals(name, "test")) {
				//
				return test;
				//
			} else if (Boolean.logicalOr(proxy instanceof FailableFunction, proxy instanceof FailableBiFunction)
					&& Objects.equals(name, "apply")) {
				//
				return null;
				//
			} else if (proxy instanceof Stream) {
				//
				if (contains(Arrays.asList("collect", "filter"), name)) {
					//
					return null;
					//
				} // if
					//
			} else if (proxy instanceof List && Objects.equals(name, "get")) {
				//
				return null;
				//
			} else if (proxy instanceof Member && Objects.equals(name, "getName")) {
				//
				return null;
				//
			} else if (proxy instanceof Entry && contains(Arrays.asList("getValue", "getKey"), name)) {
				//
				return null;
				//
			} else if (proxy instanceof VerifiableFuture && Objects.equals(name, "verify")) {
				//
				return null;
				//
			} else if (proxy instanceof SessionHolder && Objects.equals(name, "getSession")) {
				//
				return null;
				//
			} else if (proxy instanceof AuthFuture && Objects.equals(name, "isSuccess")) {
				//
				return isSuccess;
				//
			} else if (proxy instanceof ClientSession && Objects.equals(name, "auth")) {
				//
				return null;
				//
			} else if (proxy instanceof ClientSessionCreator && Objects.equals(name, "connect")) {
				//
				return null;
				//
			} else if (proxy instanceof SftpClientFactory && Objects.equals(name, "createSftpClient")) {
				//
				return null;
				//
			} else if (proxy instanceof SftpClient && contains(Arrays.asList("write", "stat", "canonicalPath"), name)) {
				//
				return null;
				//
			} else if (proxy instanceof KeyPairResourceLoader && Objects.equals(name, "loadKeyPairs")) {
				//
				return null;
				//
			} // if
				//
			throw new Throwable(name);
			//
		}

	}

	private SshServer sshServer = null;

	private IH ih = null;

	private File file = null;

	@BeforeMethod
	void beforeMethod() throws IllegalAccessException, InvocationTargetException, KeyStoreException, IOException {
		//
		if ((sshServer = SshServer.setUpDefaultServer()) != null) {
			//
			sshServer.setHost("127.0.0.1");
			//
			sshServer.setPort(2222);
			//
			sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
			//
			sshServer.setPasswordAuthenticator((username, password, session) -> Objects.equals(username, "user")
					&& Objects.equals(password, "password"));
			//
			sshServer.setCommandFactory(new ProcessShellCommandFactory());
			//
			final String name = getName(getClass(FileSystems.getDefault()));
			//
			if (contains(Arrays.asList("sun.nio.fs.MacOSXFileSystem", "sun.nio.fs.LinuxFileSystem"), name)) {
				//
				sshServer.setShellFactory(new ProcessShellFactory("/bin/sh", "-i"));
				//
			} else {
				//
				throw new IllegalStateException(name);
				//
			} // if
				//
			sshServer.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
			//
			sshServer.setFileSystemFactory(new VirtualFileSystemFactory(Paths.get("/")));
			//
			sshServer.start();
			//
		} // if
			//
		ih = new IH();
		//
		file = new File("pom.xml");
		//
	}

	@AfterMethod
	void afterMethod() throws IOException {
		//
		if (sshServer != null) {
			//
			sshServer.close();
			//
		} // if
			//
	}

	private static String getName(final Class<?> instance) {
		return instance != null ? instance.getName() : null;
	}

	private static boolean contains(final Collection<?> instance, final Object item) {
		return instance != null && instance.contains(item);
	}

	private static String getName(final Member instance) throws Throwable {
		try {
			final Object obj = invoke(METHOD_GET_NAME, null, instance);
			if (obj == null) {
				return null;
			} else if (obj instanceof String) {
				return (String) obj;
			}
			throw new Throwable(Objects.toString(getClass(instance)));
		} catch (final InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	@Test

	void testNull() throws Throwable {
		//
		final Method[] ms = SftpUploadBatch.class.getDeclaredMethods();
		//
		Method m = null;
		//
		Class<?>[] parameterTypes = null;
		//
		Class<?> parameterType = null;
		//
		Object result = null;
		//
		String toString = null;
		//
		Collection<Object> collection = null;
		//
		for (int i = 0; ms != null && i < ms.length; i++) {
			//
			if ((m = ArrayUtils.get(ms, i)) == null || m.isSynthetic()
					|| (parameterTypes = m.getParameterTypes()) == null) {
				//
				continue;
				//
			} // if
				//
			clear(collection = ObjectUtils.getIfNull(collection, ArrayList::new));
			//
			for (int j = 0; j < parameterTypes.length; j++) {
				//
				if (Objects.equals(parameterType = ArrayUtils.get(parameterTypes, j), Integer.TYPE)) {
					//
					add(collection, Integer.valueOf(0));
					//
				} else if (Objects.equals(parameterType, Character.TYPE)) {
					//
					add(collection, Character.valueOf(' '));
					//
				} else {
					//
					add(collection, null);
					//
				} // if
					//
			} // for
				//
			result = Narcissus.invokeStaticMethod(m, toArray(collection));
			//
			toString = Objects.toString(m);
			//
			if (contains(Arrays.asList(Integer.TYPE, Boolean.TYPE), m.getReturnType())) {
				//
				Assert.assertNotNull(result, toString);
				//
			} else {
				//
				Assert.assertNull(result, toString);
				//
			} // if
				//
		} // for
			//

	}

	private static <E> void add(final Collection<E> instance, final E item) {
		if (instance != null) {
			instance.add(item);
		}
	}

	private static void clear(final Collection<?> instance) {
		if (instance != null) {
			instance.clear();
		}
	}

	private static Object[] toArray(final Collection<?> instance) {
		return instance != null ? instance.toArray() : null;
	}

	@Test
	void testNotNull() throws Throwable {
		//
		final Method[] ms = SftpUploadBatch.class.getDeclaredMethods();
		//
		Method m = null;
		//
		Class<?>[] parameterTypes = null;
		//
		Class<?> parameterType = null;
		//
		Object result = null;
		//
		String toString, name = null;
		//
		Collection<Object> collection = null;
		//
		for (int i = 0; ms != null && i < ms.length; i++) {
			//
			if ((m = ArrayUtils.get(ms, i)) == null || m.isSynthetic()
					|| (parameterTypes = m.getParameterTypes()) == null) {
				//
				continue;
				//
			} // if
				//
			clear(collection = ObjectUtils.getIfNull(collection, ArrayList::new));
			//
			for (int j = 0; j < parameterTypes.length; j++) {
				//
				if ((parameterType = ArrayUtils.get(parameterTypes, j)) != null && parameterType.isInterface()) {
					//
					if ((ih = ObjectUtils.getIfNull(ih, IH::new)) != null) {
						//
						final List<Field> fs = FieldUtils.getAllFieldsList(getClass(ih));
						//
						Field f = null;
						//
						for (int k = 0; fs != null && k < fs.size(); k++) {
							//
							if ((f = fs.get(k)) == null) {
								//
								continue;
								//
							} // if
								//
							final Class<?> type = f.getType();
							//
							if (Objects.equals(type, Boolean.class)) {
								//
								Narcissus.setField(ih, f, Boolean.TRUE);
								//
							} else if (Objects.equals(type, Integer.class)) {
								//
								Narcissus.setField(ih, f, Integer.valueOf(0));
								//
							} // if
								//
						} // for
							//
					} // if
						//
					add(collection, Reflection.newProxy(parameterType, ih = ObjectUtils.getIfNull(ih, IH::new)));
					//
				} else if (parameterType != null && parameterType.isArray()) {
					//
					add(collection, Array.newInstance(parameterType.getComponentType(), 0));
					//
				} else if (Objects.equals(parameterType, Integer.TYPE)) {
					//
					add(collection, Integer.valueOf(0));
					//
				} else if (Objects.equals(parameterType, Character.TYPE)) {
					//
					add(collection, Character.valueOf(' '));
					//
				} else {
					//
					add(collection, Narcissus.allocateInstance(parameterType));
					//
				} // if
					//
			} // for
				//
			result = Narcissus.invokeStaticMethod(m, toArray(collection));
			//
			toString = Objects.toString(m);
			//
			if (contains(Arrays.asList(Boolean.TYPE, Integer.TYPE), m.getReturnType())
					|| Boolean.logicalAnd(Objects.equals(name = getName(m), "getClass"),
							Arrays.equals(parameterTypes, new Class<?>[] { Object.class }))
					|| Boolean.logicalAnd(Objects.equals(name, "append"), Boolean.logicalOr(
							Arrays.equals(parameterTypes, new Class<?>[] { StringBuilder.class, Object.class }),
							Arrays.equals(parameterTypes, new Class<?>[] { StringBuilder.class, Character.TYPE })))) {
				//
				Assert.assertNotNull(result, toString);
				//
			} else {
				//
				Assert.assertNull(result, toString);
				//
			} // if
				//
		} // for
			//
	}

	private static Class<?> getClass(final Object instance) {
		return instance != null ? instance.getClass() : null;
	}

	@Test
	void testMain() throws Exception {
		//
		SftpUploadBatch.main(new String[] { null, "=", "= ", " =", "== " });
		//
		final String host = sshServer != null ? sshServer.getHost() : null;
		//
		final String user = "user";
		//
		final RandomStringUtils randomStringUtils = RandomStringUtils.secure();
		//
		final File folder = File.createTempFile(
				randomStringUtils != null ? randomStringUtils.nextAlphanumeric(3) : null, null, new File("."));
		//
		if (folder != null && folder.exists()) {
			//
			FileUtils.copyFile(new File("pom.xml"), folder);
			//
			folder.deleteOnExit();
			//
		} // if
			//
		SftpUploadBatch.main(new String[] { "host=" + host, "port=2222", "user=" + user, "password=password",
				"file=" + getAbsolutePath(folder),
				"remoteFolder=" + getAbsolutePath(folder != null ? folder.getParentFile() : null) });
		//
		FileUtils.deleteQuietly(folder);
		//
	}

	private static String getAbsolutePath(final File instance) {
		return instance != null ? instance.getAbsolutePath() : null;
	}

	private static Object invoke(final Method method, final Object instance, final Object... args)
			throws IllegalAccessException, InvocationTargetException {
		return method != null && method.getDeclaringClass() != null ? method.invoke(instance, args) : null;
	}

	@Test
	void testIsSuccess() throws IllegalAccessException, InvocationTargetException {
		//
		if ((ih = ObjectUtils.getIfNull(ih, IH::new)) != null) {
			//
			ih.isSuccess = Boolean.FALSE;
			//
		} // if
			//
		Assert.assertEquals(invoke(METHOD_IS_SUCCESS, null, Reflection.newProxy(AuthFuture.class, ih)),
				ih != null ? ih.isSuccess : null);
		//
	}

	@Test
	void testCollect() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNull(invoke(METHOD_COLLECT, null, Stream.empty(), null));
		//
		Assert.assertNull(invoke(METHOD_COLLECT, null, Reflection.newProxy(Stream.class, ih), null));
		//
	}

	@Test
	void testExists() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertEquals(invoke(METHOD_EXISTS, null, file), Boolean.TRUE);
		//
		Assert.assertNotNull(invoke(METHOD_EXISTS, null, new File("")));
		//
	}

	@Test
	void testIsFile() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertEquals(invoke(METHOD_IS_FILE, null, file), Boolean.TRUE);
		//
		Assert.assertEquals(invoke(METHOD_IS_FILE, null, new File("")), Boolean.FALSE);
		//
	}

	@Test
	void testToPath() throws IllegalAccessException, InvocationTargetException {
		//
		Assert.assertNotNull(invoke(METHOD_TO_PATH, null, file));
		//
	}

}