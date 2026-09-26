package org.apache.sshd.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Proxy;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableBiFunction;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.client.session.ClientSessionCreator;
import org.apache.sshd.common.auth.BasicCredentialsImpl;
import org.apache.sshd.common.auth.BasicCredentialsProvider;
import org.apache.sshd.common.auth.UsernameHolder;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader;
import org.apache.sshd.common.future.VerifiableFuture;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.common.session.SessionContext;
import org.apache.sshd.common.session.SessionHolder;
import org.apache.sshd.putty.PuttyKeyUtils;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClient.Attributes;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.net.HostAndPort;

import io.github.toolfactory.narcissus.Narcissus;

public class SftpUploadBatch {

	private static final Logger LOG = LoggerFactory.getLogger(SftpUploadBatch.class);

	private static final String VALUE = "value";

	public static void main(final String[] args) throws Exception {
		//
		final Map<String, String> map = toMap(args);
		//
		if (containsKey(map, "config")) {
			//
			perform(parse(newDocumentBuilder(DocumentBuilderFactory.newInstance()),
					testAndApply(Objects::nonNull, get(map, "config"), File::new, null)),
					newXPath(XPathFactory.newInstance()));
			//
		} else {
			//
			info(LOG, perform(
					testAndApply(Objects::nonNull, get(map, "host"),
							x -> HostAndPort.fromParts(x, NumberUtils.toInt(get(map, "port"), 22)), null),
					new BasicCredentialsImpl(get(map, "user"), get(map, "password")),
					testAndApply(x -> size(x) == 1,
							testAndApply(x -> Boolean.logicalAnd(exists(x), isFile(x)),
									testAndApply(Objects::nonNull, get(map, "key"), File::new, null),
									x -> loadKeyPairs(PuttyKeyUtils.DEFAULT_INSTANCE, null, toPath(x), null), null),
							x -> new ArrayList<>(x).get(0), null),
					testAndApply(Objects::nonNull, get(map, "file"), File::new, null), get(map, "remoteFolder")));
			//
		} // if
			//
	}

	private static XPath newXPath(final XPathFactory instance) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), "_featureManager")), Collectors.toList()),
				x -> get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.newXPath() : null;
		//
	}

	private static Document parse(final DocumentBuilder instance, final File file) throws SAXException, IOException {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), "domParser")), Collectors.toList()),
				x -> get(x, 0), null);
		//
		return (field == null || Narcissus.getField(instance, field) != null) && file != null && file.getPath() != null
				&& exists(file) && file.isFile() ? instance.parse(file) : null;
		//
	}

	private static DocumentBuilder newDocumentBuilder(final DocumentBuilderFactory instance)
			throws ParserConfigurationException {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = testAndApply(x -> size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), "fSecurityManager")), Collectors.toList()),
				x -> get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.newDocumentBuilder() : null;
		//
	}

	private static boolean containsKey(final Map<?, ?> instance, final Object key) {
		return instance != null && instance.containsKey(key);
	}

	private static void info(final Logger logger, final Result result) {
		//
		final HostAndPort hostAndPort = result != null ? result.hostAndPort : null;
		//
		info(logger, "Host       ={}", getHost(hostAndPort));
		//
		info(logger, "Port       ={}", getPort(hostAndPort));
		//
		info(logger, "User       ={}", getUsername(result != null ? result.usernameHolder : null));
		//
		info(logger, "Path       ={}", result != null ? result.canonicalPath : null);
		//
		info(logger, "Size       ={}", result != null ? result.copy : null);
		//
		final Attributes stat = result != null ? result.stat : null;
		//
		info(logger, "Create Time={}", getCreateTime(stat));
		//
		info(logger, "Modify Time={}", getModifyTime(stat));
		//

	}

	private static String getUsername(final UsernameHolder instance) {
		return instance != null ? instance.getUsername() : null;
	}

	private static String getHost(final HostAndPort instance) {
		return instance != null ? instance.getHost() : null;
	}

	private static Integer getPort(final HostAndPort instance) {
		return instance != null && instance.hasPort() ? Integer.valueOf(instance.getPort()) : null;
	}

	private static class Result {

		private HostAndPort hostAndPort = null;

		private UsernameHolder usernameHolder = null;

		private String canonicalPath = null;

		private Integer copy = null;

		private Attributes stat = null;

	}

	private static void perform(final Document document, final XPath xp) throws Exception {
		//
		final JFileChooser jfc = new JFileChooser();
		//
		File file = null;
		//
		if ((file = testAndApply(Objects::nonNull,
				getNodeValue(getNamedItem(
						getAttributes(cast(Node.class, evaluate(xp, "/*/file", document, XPathConstants.NODE))),
						"value")),
				File::new, null)) == null && !isTestMode() && jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			//
			file = jfc.getSelectedFile();
			//
		} // if
			//
		String remoteFolder = null;
		//
		if (StringUtils.isEmpty(remoteFolder = getNodeValue(getNamedItem(
				getAttributes(cast(Node.class, evaluate(xp, "/*/remoteFolder", document, XPathConstants.NODE))),
				"value")))) {
			//
			remoteFolder = !isTestMode() ? JOptionPane.showInputDialog(null, "Remote Folder", remoteFolder) : null;
			//
		} // if
			//
		final NodeList nodeList = cast(NodeList.class, evaluate(xp, "/*/*/host", document, XPathConstants.NODESET));
		//
		Node node = null;
		//
		for (int i = 0; nodeList != null && i < nodeList.getLength(); i++) {
			//
			if ((node = nodeList.item(i)) == null) {
				//
				continue;
				//
			} // if
				//
			final Node n = node;
			//
			info(LOG, perform(
					testAndApply(Objects::nonNull, getNodeValue(getNamedItem(getAttributes(node), "host")),
							x -> HostAndPort.fromParts(x,
									NumberUtils.toInt(getNodeValue(getNamedItem(getAttributes(n), "port")), 22)),
							null),
					new BasicCredentialsImpl(getNodeValue(getNamedItem(getAttributes(node), "user")),
							getNodeValue(getNamedItem(getAttributes(node), "password"))),
					testAndApply(x -> size(x) == 1,
							testAndApply(x -> Boolean.logicalAnd(exists(x), isFile(x)),
									testAndApply(Objects::nonNull,
											getNodeValue(getNamedItem(getAttributes(node), "key")), File::new, null),
									x -> loadKeyPairs(PuttyKeyUtils.DEFAULT_INSTANCE, null, toPath(x), null), null),
							x -> new ArrayList<>(x).get(0), null),
					file, remoteFolder));
			//
		} // for
			//
	}

	private static boolean isTestMode() {
		try {
			return Class.forName("org.testng.annotations.Test") != null;
		} catch (final ClassNotFoundException e) {
			return false;
		}
	}

	private static Node getNamedItem(final NamedNodeMap instance, final String name) {
		return instance != null ? instance.getNamedItem(name) : null;
	}

	private static String getNodeValue(final Node instance) {
		return instance != null ? instance.getNodeValue() : null;
	}

	private static NamedNodeMap getAttributes(final Node instance) {
		return instance != null ? instance.getAttributes() : null;
	}

	private static <T> T cast(final Class<T> clz, final Object instance) {
		return clz != null && clz.isInstance(instance) ? clz.cast(instance) : null;
	}

	private static Object evaluate(final XPath instance, final String string, final Object object, final QName qName)
			throws XPathExpressionException {
		return instance != null && object != null ? instance.evaluate(string, object, qName) : null;
	}

	private static Result perform(final HostAndPort hostAndPort,
			final BasicCredentialsProvider basicCredentialsProvider, final KeyPair keyPair, final File file,
			final String remoteFolderString) throws IOException {
		//
		Result result = null;
		//
		try (final SshClient sshClient = SshClient.setUpDefaultClient()) {
			//
			setServerKeyVerifier(sshClient, AcceptAllServerKeyVerifier.INSTANCE);
			//
			start(sshClient);
			//
			try (final ClientSession clientSession = testAndApply(
					(a, b) -> Boolean.logicalAnd(a != null, StringUtils.isNotEmpty(b)),
					getUsername(basicCredentialsProvider), hostAndPort != null ? hostAndPort.getHost() : null, (a,
							b) -> getSession(verify(connect(sshClient, a, b,
									hostAndPort != null && hostAndPort.hasPort() ? hostAndPort.getPort() : 22))),
					null)) {
				//
				testAndAccept(Objects::nonNull,
						basicCredentialsProvider != null ? basicCredentialsProvider.getPassword() : null,
						x -> addPasswordIdentity(clientSession, x));
				//
				testAndAccept(Objects::nonNull, keyPair, x -> addPublicKeyIdentity(clientSession, x));
				//
				final Field field = testAndApply(x -> size(x) == 1,
						collect(filter(stream(testAndApply(Objects::nonNull, getClass(remoteFolderString),
								FieldUtils::getAllFieldsList, null)), f -> Objects.equals(getName(f), VALUE)),
								Collectors.toList()),
						x -> get(x, 0), null);
				//
				final StringBuilder remoteFolder = testAndApply(
						x -> x != null && (field == null || Narcissus.getField(remoteFolderString, field) != null),
						remoteFolderString, StringBuilder::new, null);
				//
				append(append(remoteFolder, '/'), getName(file));
				//
				try (final SftpClient sftpClient = isSuccess(verify(auth(clientSession)))
						? createSftpClient(SftpClientFactory.instance(), clientSession)
						: null;
						final InputStream is = testAndApply(x -> x != null && x.getPath() != null, file,
								FileInputStream::new, null);
						final OutputStream os = file != null ? write(sftpClient, Objects.toString(remoteFolder))
								: null) {
					//
					(result = new Result()).hostAndPort = hostAndPort;
					//
					result.usernameHolder = basicCredentialsProvider;
					//
					result.copy = testAndApply((a, b) -> Boolean.logicalAnd(a != null, b != null), is, os,
							IOUtils::copy, null);
					//
					result.canonicalPath = canonicalPath(sftpClient, Objects.toString(remoteFolder));
					//
					result.stat = file != null ? stat(sftpClient, Objects.toString(remoteFolder)) : null;
					//
				} // try
					//
			} // try
				//
		} // try
			//
		return result;
	}

	private static void info(final Logger instance, final String format, final Object object) {
		if (instance != null) {
			instance.info(format, object);
		}
	}

	private static FileTime getModifyTime(final Attributes instance) {
		return instance != null ? instance.getModifyTime() : null;
	}

	private static FileTime getCreateTime(final Attributes instance) {
		return instance != null ? instance.getCreateTime() : null;
	}

	private static Attributes stat(final SftpClient instance, final String path) throws IOException {
		return instance != null ? instance.stat(path) : null;
	}

	private static String canonicalPath(final SftpClient instance, final String path) throws IOException {
		return instance != null ? instance.canonicalPath(path) : null;
	}

	private static Collection<KeyPair> loadKeyPairs(final KeyPairResourceLoader instance, final SessionContext session,
			final Path path, final FilePasswordProvider passwordProvider, final OpenOption... options)
			throws IOException, GeneralSecurityException {
		return instance != null ? instance.loadKeyPairs(session, path, passwordProvider, options) : null;
	}

	private static boolean exists(final File instance) {
		return instance != null && instance.getPath() != null && instance.exists();
	}

	private static boolean isFile(final File instance) {
		return instance != null && instance.getPath() != null && instance.isFile();
	}

	private static Path toPath(final File instance) {
		return instance != null && instance.getPath() != null ? instance.toPath() : null;
	}

	private static void addPublicKeyIdentity(final ClientAuthenticationManager instance, final KeyPair keyPair) {
		if (instance != null) {
			instance.addPublicKeyIdentity(keyPair);
		}
	}

	private static StringBuilder append(final StringBuilder instance, final char c) {
		//
		if (instance == null) {
			//
			return instance;
			//
		} // if
			//
		final Field field = testAndApply(x -> size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), VALUE)), Collectors.toList()),
				x -> get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.append(c) : instance;
		//
	}

	private static StringBuilder append(final StringBuilder instance, final Object obj) {
		//
		if (instance == null) {
			//
			return instance;
			//
		} // if
			//
		final Field field = testAndApply(x -> size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), VALUE)), Collectors.toList()),
				x -> get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.append(obj) : instance;
		//
	}

	private static String getName(final File instance) {
		return instance != null && instance.getPath() != null ? instance.getName() : null;
	}

	private static OutputStream write(final SftpClient instance, final String path) throws IOException {
		return instance != null ? instance.write(path) : null;
	}

	private static void setServerKeyVerifier(final ClientAuthenticationManager instance,
			final ServerKeyVerifier serverKeyVerifier) {
		if (instance != null) {
			instance.setServerKeyVerifier(serverKeyVerifier);
		}
	}

	private static <T, U, R, E extends Exception> R testAndApply(final BiPredicate<T, U> predicate, final T t,
			final U u, final FailableBiFunction<T, U, R, E> functionTrue,
			final FailableBiFunction<T, U, R, E> functionFalse) throws E {
		return predicate != null && predicate.test(t, u) ? apply(functionTrue, t, u) : apply(functionFalse, t, u);
	}

	private static <T, U, R, E extends Exception> R apply(final FailableBiFunction<T, U, R, E> instance, final T t,
			final U u) throws E {
		return instance != null ? instance.apply(t, u) : null;
	}

	private static SftpClient createSftpClient(final SftpClientFactory instnace, final ClientSession session)
			throws IOException {
		return instnace != null ? instnace.createSftpClient(session) : null;
	}

	private static ConnectFuture connect(final ClientSessionCreator instance, final String username, final String host,
			final int port) throws IOException {
		return instance != null ? instance.connect(username, host, port) : null;
	}

	private static AuthFuture auth(final ClientSession instance) throws IOException {
		return instance != null ? instance.auth() : null;
	}

	private static <T, E extends Exception> void testAndAccept(final Predicate<T> predicate, final T value,
			final FailableConsumer<T, E> consumer) throws E {
		if (test(predicate, value)) {
			accept(consumer, value);
		}
	}

	private static <T, E extends Exception> void accept(final FailableConsumer<T, E> instance, final T value) throws E {
		if (instance != null) {
			instance.accept(value);
		}
	}

	private static void addPasswordIdentity(final ClientAuthenticationManager instance, final String password) {
		if (instance != null) {
			instance.addPasswordIdentity(password);
		}
	}

	private static void start(final SshClient instance) {
		//
		if (instance == null) {
			//
			return;
			//
		} // if
			//
		final Field field = testAndApply(x -> size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), "state")), Collectors.toList()),
				x -> get(x, 0), null);
		//
		if (field == null || (Narcissus.getField(instance, field)) != null) {
			//
			instance.start();
			//
		} // if
			//
	}

	private static boolean isSuccess(final AuthFuture instance) {
		return instance != null && instance.isSuccess();
	}

	private static <S extends Session> S getSession(final SessionHolder<S> instance) {
		return instance != null ? instance.getSession() : null;
	}

	private static <T> T verify(final VerifiableFuture<T> instnace) throws IOException {
		return instnace != null ? instnace.verify() : null;
	}

	private static <V> V get(final Map<?, V> instance, final Object key) {
		return instance != null ? instance.get(key) : null;
	}

	private static Map<String, String> toMap(final String... ss) {
		//
		Map<String, String> map = null;
		//
		Entry<String, String> entry = null;
		//
		for (int i = 0; i < length(ss); i++) {
			//
			if ((entry = toEntry(ArrayUtils.get(ss, i))) == null) {
				//
				continue;
				//
			} // if
				//
			put(map = ObjectUtils.getIfNull(map, LinkedHashMap::new), getKey(entry), getValue(entry));
			//
		} // for
			//
		return map;
		//
	}

	private static Entry<String, String> toEntry(final String string) {
		//
		final Field field = testAndApply(x -> size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(string), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), VALUE)), Collectors.toList()),
				x -> get(x, 0), null);
		//
		if (string != null && field != null && Narcissus.getField(string, field) == null) {
			//
			return null;
			//
		} // if
			//
		if (Objects.equals(string, "=")) {
			//
			return Pair.of("", "");
			//
		} else if (string != null && string.length() == 2 && string.charAt(0) == '=') {
			//
			return Pair.of("", string.substring(1, string.length()));
			//
		} else if (string != null && string.length() == 2 && string.charAt(string.length() - 1) == '=') {
			//
			return Pair.of(string.substring(0, string.length() - 1), "");
			//
		} else if (string != null && string.indexOf('=') >= 0 && string.indexOf('=') == string.lastIndexOf('=')) {
			//
			return Pair.of(StringUtils.substringBefore(string, '='), StringUtils.substringAfter(string, '='));
			//
		} else if (string != null && string.length() > 2 && string.indexOf('=') != string.lastIndexOf('=')) {
			//
			return Pair.of(StringUtils.substring(string, 0, string.indexOf('=')),
					StringUtils.substring(string, string.indexOf('=') + 1));
			//
		} // if
			//
		return null;
		//
	}

	private static int length(final Object[] instance) {
		return instance != null ? instance.length : 0;
	}

	private static <K> K getKey(final Entry<K, ?> instance) {
		return instance != null ? instance.getKey() : null;
	}

	private static <V> V getValue(final Entry<?, V> instance) {
		return instance != null ? instance.getValue() : null;
	}

	private static <K, V> void put(final Map<K, V> instance, final K key, final V value) {
		if (instance != null) {
			instance.put(key, value);
		}
	}

	private static int size(final Collection<?> instance) {
		return instance != null ? instance.size() : 0;
	}

	private static <E> E get(final List<E> instance, final int index) {
		return instance != null ? instance.get(index) : null;
	}

	private static String getName(final Member instance) {
		return instance != null ? instance.getName() : null;
	}

	private static <T, R, A> R collect(final Stream<T> instance, final Collector<? super T, A, R> collector) {
		return instance != null && (collector != null || Proxy.isProxyClass(getClass(instance)))
				? instance.collect(collector)
				: null;
	}

	private static <T> Stream<T> filter(final Stream<T> instance, final Predicate<? super T> predicate) {
		return instance != null ? instance.filter(predicate) : instance;
	}

	private static <T> Stream<T> stream(final Collection<T> instance) {
		return instance != null ? instance.stream() : null;
	}

	private static Class<?> getClass(final Object instance) {
		return instance != null ? instance.getClass() : null;
	}

	private static <T, R, E extends Throwable> R testAndApply(final Predicate<T> predicate, final T value,
			final FailableFunction<T, R, E> functionTrue, final FailableFunction<T, R, E> functionFalse) throws E {
		return test(predicate, value) ? apply(functionTrue, value) : apply(functionFalse, value);
	}

	private static <T> boolean test(final Predicate<T> instance, final T value) {
		return instance != null && instance.test(value);
	}

	private static <T, R, E extends Throwable> R apply(final FailableFunction<T, R, E> instance, final T value)
			throws E {
		return instance != null ? instance.apply(value) : null;
	}

}