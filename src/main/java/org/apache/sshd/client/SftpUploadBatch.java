package org.apache.sshd.client;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.MutableComboBoxModel;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.FieldOrMethod;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.IFLT;
import org.apache.bcel.generic.IF_ICMPLE;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.MethodGen;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableBiFunction;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.stream.Streams.FailableStream;
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
import com.sun.jna.platform.win32.Kernel32Util;

import io.github.toolfactory.narcissus.Narcissus;
import net.miginfocom.swing.MigLayout;

public class SftpUploadBatch extends JPanel implements ActionListener, ItemListener {

	private static final long serialVersionUID = -7062996438496794210L;

	private static final Logger LOG = LoggerFactory.getLogger(SftpUploadBatch.class);

	private static final String VALUE = "value";

	private static final String PASSWORD = "password";

	private static final String OBJECT_LOCK = "objectLock";

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface Note {
		String value();
	}

	@Note("Host")
	private JTextComponent tfHost = null;

	@Note("Port")
	private JTextComponent tfPort = null;

	@Note("User")
	private JTextComponent tfUser = null;

	@Note("Password")
	private JTextComponent tfPassword = null;

	@Note("Key")
	private JTextComponent tfKey = null;

	@Note("File")
	private JTextComponent tfFile = null;

	private JTextComponent tfRemoteFolder = null;

	@Note("Key")
	private AbstractButton btnKey = null;

	@Note("File")
	private AbstractButton btnFile = null;

	private AbstractButton btnExecute = null;

	private JComboBox<Node> jcb = null;

	private SftpUploadBatch() {
		//
	}

	public static void main(final String[] args) throws Exception {
		//
		final Map<String, String> map = toMap(args);
		//
		if (isGui(map)) {
			//
			final SftpUploadBatch instance = new SftpUploadBatch();
			//
			instance.setLayout(new MigLayout());
			//
			add(instance, new JLabel("Hosts"));
			//
			final DefaultComboBoxModel<Node> dcbm = new DefaultComboBoxModel<>();
			//
			final XPath xp = newXPath(XPathFactory.newInstance());
			//
			final NodeList nodeList = cast(NodeList.class, evaluate(xp, "/*/host",
					parse(newDocumentBuilder(DocumentBuilderFactory.newInstance()), new File("sftp-upload-batch.xml")),
					XPathConstants.NODESET));
			//
			forEach(IntStream.range(0, getLength(nodeList)), i -> dcbm.addElement(item(nodeList, i)));
			//
			testAndAccept(x -> getSize(x) == 1, dcbm, x -> {
				//
				insertElementAt(x, null, 0);
				//
				setSelectedItem(x, null);
				//
			});
			//
			final ListCellRenderer<?> render = (instance.jcb = new JComboBox<Node>(dcbm)).getRenderer();
			//
			final List<Method> ms = collect(
					stream(new FailableStream<>(testAndApply(Objects::nonNull,
							ListCellRenderer.class.getDeclaredMethods(), Arrays::stream, null)).filter(
									m -> Boolean.logicalAnd(Objects.equals(getName(m), "getListCellRendererComponent"),
											Arrays.equals(getParameterTypes(m), new Class<?>[] { JList.class,
													Object.class, Integer.TYPE, Boolean.TYPE, Boolean.TYPE })))),
					Collectors.toList());
			//
			final Method method = testAndApply(x -> size(x) == 1, ms, x -> get(x, 0), null);
			//
			instance.jcb.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
				//
				final Component component = cast(Component.class,
						Narcissus.invokeMethod(render, method, list, value, index, isSelected, cellHasFocus));
				//
				if (value != null) {
					//
					final StringBuilder sb = new StringBuilder(
							StringUtils.defaultString(getNodeValue(getNamedItem(getAttributes(value), "host"))));
					//
					sb.append(':');
					//
					setText(cast(JLabel.class, component), Objects.toString(sb
							.append(Objects.toString(getNodeValue(getNamedItem(getAttributes(value), "port")), "22"))));
					//
				} // if
					//
				return component;
				//
			});
			//
			instance.jcb.addItemListener(instance);
			//
			final String wrap = "wrap";
			//
			instance.add(instance.jcb, wrap);
			//
			add(instance, new JLabel("Host"));
			//
			instance.add(instance.tfHost = new JTextField(), String.format("%1$s,wmin %2$s", wrap, 100));
			//
			add(instance, new JLabel("Port"));
			//
			final String growx = "growx";
			//
			instance.add(instance.tfPort = new JTextField(), StringUtils.joinWith(",", growx, wrap));
			//
			final int[] ports = getPorts();
			//
			final int maxPortNumber = testAndApplyAsInt(x -> length(x) > 0, ports, NumberUtils::max, null, 0);
			//
			final int maxPortStringLength = StringUtils.length(Integer.toString(maxPortNumber));
			//
			instance.tfPort.setDocument(new PlainDocument() {
				@Override
				public void insertString(final int offset, final String string, final AttributeSet attributeSet)
						throws BadLocationException {
					//
					if ((getLength() + StringUtils.length(string)) <= maxPortStringLength) {
						//
						final StringBuilder sb = testAndApply(Objects::nonNull,
								SftpUploadBatch.getText(instance.tfPort), StringBuilder::new, null);
						//
						if (sb != null) {
							//
							sb.insert(offset, string);
							//
						} // if
							//
						if (NumberUtils.isDigits(Objects.toString(sb))
								&& NumberUtils.toInt(Objects.toString(sb)) > maxPortNumber) {
							//
							return;
							//
						} // if
							//
						super.insertString(offset, string, attributeSet);
						//
					} // if
						//
				}
			});
			//
			testAndRun(testAndApplyAsInt(x -> length(x) > 0, ports, NumberUtils::min, null, 0) == 0,
					() -> instance.tfPort.addKeyListener(createKeyListener()));
			//
			add(instance, new JLabel("User"));
			//
			instance.add(instance.tfUser = new JTextField(), StringUtils.joinWith(",", growx, wrap));
			//
			add(instance, new JLabel(StringUtils.capitalize(PASSWORD)));
			//
			instance.add(instance.tfPassword = new JPasswordField(), StringUtils.joinWith(",", growx, wrap));
			//
			add(instance, new JLabel("Key"));
			//
			instance.add(instance.tfKey = new JTextField(), growx);
			//
			instance.add(instance.btnKey = new JButton("Choose Key"), wrap);
			//
			add(instance, new JLabel("File"));
			//
			instance.add(instance.tfFile = new JTextField(), growx);
			//
			instance.add(instance.btnFile = new JButton("Choose File"), wrap);
			//
			add(instance, new JLabel("Remote Folder"));
			//
			instance.add(instance.tfRemoteFolder = new JTextField(), StringUtils.joinWith(",", growx, wrap));
			//
			add(instance, new JLabel());
			//
			instance.add(instance.btnExecute = new JButton("Upload"), wrap);
			//
			forEach(Arrays.asList(instance.tfKey, instance.tfFile), x -> setEditable(x, false));
			//
			forEach(Arrays.asList(instance.btnKey, instance.btnFile, instance.btnExecute),
					x -> addActionListener(x, instance));
			//
			final JFrame jFrame = testAndGet(!GraphicsEnvironment.isHeadless(), JFrame::new);
			//
			setDefaultCloseOperation(jFrame, WindowConstants.EXIT_ON_CLOSE);
			//
			add(jFrame, instance);
			//
			pack(jFrame);
			//
			testAndRun(!isTestMode(), () -> setVisible(jFrame, true));
			//
			return;
			//
		} // if
			//
		perform(map);
		//
	}

	private static void perform(final Map<?, String> map) throws RuntimeException, Exception {
		//
		final File file = testAndApply(Objects::nonNull, get(map, "file"), File::new, null);
		//
		final String remoteFolder = get(map, "remoteFolder");
		//
		if (containsKey(map, "config")) {
			//
			perform(parse(newDocumentBuilder(DocumentBuilderFactory.newInstance()),
					testAndApply(Objects::nonNull, get(map, "config"), File::new, null)),
					newXPath(XPathFactory.newInstance()), file, remoteFolder);
			//
		} else {
			//
			info(LOG,
					perform(testAndApply(Objects::nonNull, get(map, "host"),
							x -> HostAndPort.fromParts(x, NumberUtils.toInt(get(map, "port"), 22)), null),
							new BasicCredentialsImpl(get(map, "user"), get(map, PASSWORD)),
							testAndApply(x -> size(x) == 1, testAndApply(x -> Boolean.logicalAnd(exists(x), isFile(x)),
									testAndApply(Objects::nonNull, get(map, "key"), File::new, null),
									x -> loadKeyPairs(PuttyKeyUtils.DEFAULT_INSTANCE, null, toPath(x), null), null),
									x -> new ArrayList<>(x).get(0), null),
							file, remoteFolder));
			//
		} // if
			//
	}

	@Override
	public void actionPerformed(final ActionEvent evt) {
		//
		final Object source = getSource(evt);
		//
		if (Objects.equals(source, btnKey)) {
			//
			showOpenDialogAndSetText(new File("."), tfKey);
			//
		} else if (Objects.equals(source, btnFile)) {
			//
			showOpenDialogAndSetText(new File("."), tfFile);
			//
		} else if (Objects.equals(source, btnExecute)) {
			//
			try {
				//
				info(LOG, perform(
						testAndApply(Objects::nonNull, getText(tfHost),
								x -> HostAndPort.fromParts(x, NumberUtils.toInt(getText(tfPort), 22)), null),
						new BasicCredentialsImpl(getText(tfUser), getText(tfPassword)),
						testAndApply(x -> size(x) == 1,
								testAndApply(x -> Boolean.logicalAnd(exists(x), isFile(x)),
										testAndApply(Objects::nonNull, getText(tfKey), File::new, null),
										x -> loadKeyPairs(PuttyKeyUtils.DEFAULT_INSTANCE, null, toPath(x), null), null),
								x -> new ArrayList<>(x).get(0), null),
						testAndApply(Objects::nonNull, getText(tfFile), File::new, null), getText(tfRemoteFolder)));
				//
			} catch (final Exception e) {
				//
				throw new RuntimeException(e);
				//
			} // try
				//
		} // if
			//
	}

	@Override
	public void itemStateChanged(final ItemEvent evt) {
		//
		if (Objects.equals(getSource(evt), jcb) && evt != null) {
			//
			final int stateChange = evt.getStateChange();
			//
			if (stateChange == ItemEvent.SELECTED) {
				//
				final Node node = cast(Node.class, evt.getItem());
				//
				setText(tfHost, getNodeValue(getNamedItem(getAttributes(node), "host")));
				//
				setText(tfPort, getNodeValue(getNamedItem(getAttributes(node), "port")));
				//
				setText(tfUser, getNodeValue(getNamedItem(getAttributes(node), "user")));
				//
				setText(tfPassword, getNodeValue(getNamedItem(getAttributes(node), PASSWORD)));
				//
				setText(tfKey, getNodeValue(getNamedItem(getAttributes(node), "key")));
				//
			} else if (stateChange == ItemEvent.DESELECTED && getSelectedItem(jcb) == null) {
				//
				forEach(Arrays.asList(tfHost, tfPort, tfUser, tfPassword, tfKey), x -> setText(x, null));
				//
			} // if
				//
		} // if
			//
	}

	private static Object getSelectedItem(final JComboBox<?> instance) {
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
						x -> Objects.equals(getName(x), "dataModel")), Collectors.toList()),
				x -> get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.getSelectedItem() : null;
		//
	}

	private static void showOpenDialogAndSetText(final File file, final JTextComponent jtc) {
		//
		JFileChooser jfc = null;
		//
		try {
			//
			jfc = new JFileChooser(getCanonicalFile(file));
			//
		} catch (final IOException e) {
			//
			throw new RuntimeException(e);
			//
		} // try
			//
		if (jfc != null && !GraphicsEnvironment.isHeadless() && !isTestMode()) {
			//
			final int showOpenDialog = jfc.showOpenDialog(null);
			//
			if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
				//
				setText(jtc, getAbsolutePath(jfc.getSelectedFile()));
				//
			} else if (showOpenDialog == JFileChooser.CANCEL_OPTION) {
				//
				setText(jtc, null);
				//
			} // if
				//
		} // if
			//
	}

	private static boolean isGui(final Map<?, String> map) {
		//
		boolean gui = false;
		//
		if (containsKey(map, "gui")) {
			//
			gui = BooleanUtils.toBooleanDefaultIfNull(Boolean.valueOf(get(map, "gui")), gui);
			//
		} // if
			//
		final String name = getName(getClass(FileSystems.getDefault()));
		//
		if (Boolean.logicalAnd(Objects.equals(name, "sun.nio.fs.MacOSXFileSystem"), !isTestMode())) {
			//
			gui = System.console() == null;
			//
		} // if
			//
		if (Boolean.logicalAnd(!gui, Objects.equals(name, "sun.nio.fs.WindowsFileSystem"))) {
			//
			final Matcher matcher = matcher(Pattern.compile("\\d+"), getName(ManagementFactory.getRuntimeMXBean()));
			//
			if (find(matcher)) {
				//
				gui = BooleanUtils.toBooleanDefaultIfNull(testAndApply(NumberUtils::isDigits, group(matcher),
						x -> endsWith(Kernel32Util.QueryFullProcessImageName(NumberUtils.toInt(x), 0), "javaw.exe"),
						null), false);
				//
			} // if
				//
		} // if
			//
		return gui;
		//
	}

	private static KeyListener createKeyListener() {
		//
		return new KeyAdapter() {

			@Override
			public void keyTyped(final KeyEvent evt) {
				//
				if (evt == null) {
					//
					return;
					//
				} // if
					//
				final char c = evt.getKeyChar();
				//
				if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
					//
					evt.consume();
					//
				} // if
					//
			}

		};
		//
	}

	private static <T> int testAndApplyAsInt(final Predicate<T> predicate, final T value,
			final ToIntFunction<T> functionTrue, final ToIntFunction<T> functionFalse, final int defautlValue) {
		return test(predicate, value) ? applyAsInt(functionTrue, value, defautlValue)
				: applyAsInt(functionFalse, value, defautlValue);
	}

	private static <T> int applyAsInt(final ToIntFunction<T> instance, final T value, final int defautlValue) {
		return instance != null ? instance.applyAsInt(value) : defautlValue;
	}

	private static int[] getPorts() throws ClassFormatException, IOException {
		//
		int[] ints = null;
		//
		org.apache.bcel.classfile.Method checkPort = null;
		//
		try (final InputStream is = SftpUploadBatch.class.getResourceAsStream(
				StringUtils.join('/', replace(getName(InetSocketAddress.class), '.', '/'), ".class"))) {
			//
			final org.apache.bcel.classfile.Method[] methods = getMethods(
					parse(testAndApply(Objects::nonNull, is, x -> new ClassParser(x, null), null)));
			//
			org.apache.bcel.classfile.Method m = null;
			//
			for (int i = 0; i < length(methods); i++) {
				//
				if (!Objects.equals(getName(m = ArrayUtils.get(methods, i)), "checkPort")) {
					//
					continue;
					//
				} // if
					//
				testAndRun(checkPort != null, () -> {
					//
					throw new IllegalStateException();
					//
				});
				//
				checkPort = m;
				//
			} // for
				//
		} // try
			//
		final InstructionHandle[] ihs = getInstructionHandles(
				new MethodGen(checkPort, null, null).getInstructionList());
		//
		Instruction instruction = null;
		//
		ConstantPoolGen cpg = null;
		//
		for (int i = 0; i < length(ihs); i++) {
			//
			if ((instruction = getInstruction(ArrayUtils.get(ihs, i))) instanceof IFLT) {
				//
				ints = ArrayUtils.add(ints, 0);
				//
			} else if (instruction instanceof IF_ICMPLE && i > 0
					&& (instruction = getInstruction(ArrayUtils.get(ihs, i - 1))) instanceof LDC) {
				//
				if (cpg == null) {
					//
					cpg = testAndApply(Objects::nonNull, getConstantPool(checkPort), ConstantPoolGen::new, null);
					//
				} // if
					//
				ints = ArrayUtils.add(ints, intValue(cast(Number.class, ((LDC) instruction).getValue(cpg)), 0));
				//
			} // if
				//
		} // for
			//
		return ints;
		//
	}

	private static JavaClass parse(final ClassParser instance) throws ClassFormatException, IOException {
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
						f -> Objects.equals(getName(f), "dataInputStream")), Collectors.toList()),
				x -> get(x, 0), null);
		//
		return (field == null || Narcissus.getField(instance, field) != null) ? instance.parse() : null;
		//
	}

	private static void setVisible(final Component instnace, final boolean visible) {
		if (instnace != null) {
			instnace.setVisible(visible);
		}
	}

	private static void pack(final Window instance) {
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
						f -> Objects.equals(getName(f), OBJECT_LOCK)), Collectors.toList()),
				x -> get(x, 0), null);
		//
		if (field == null || Narcissus.getField(instance, field) != null) {
			//
			instance.pack();
			//
		} // if
			//
	}

	private static void add(final Container instance, final Component comp) {
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
						x -> Objects.equals(getName(x), "component")), Collectors.toList()),
				x -> get(x, 0), null);
		//
		if (field == null || Narcissus.getField(instance, field) != null) {
			//
			instance.add(comp);
			//
		} // if
			//
	}

	private static void setDefaultCloseOperation(final JFrame instance, final int operation) {
		if (instance != null) {
			instance.setDefaultCloseOperation(operation);
		}
	}

	private static String getName(final FieldOrMethod instance) {
		return instance != null && instance.getConstantPool() != null ? instance.getName() : null;
	}

	private static ConstantPool getConstantPool(final FieldOrMethod instnace) {
		return instnace != null ? instnace.getConstantPool() : null;
	}

	private static InstructionHandle[] getInstructionHandles(final InstructionList instance) {
		return instance != null ? instance.getInstructionHandles() : null;
	}

	private static org.apache.bcel.classfile.Method[] getMethods(final JavaClass instance) {
		return instance != null ? instance.getMethods() : null;
	}

	private static Instruction getInstruction(final InstructionHandle instance) {
		return instance != null ? instance.getInstruction() : null;
	}

	private static String replace(final String instance, final char oldChar, final char newChar) {
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
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.replace(oldChar, newChar) : null;
		//
	}

	private static int intValue(final Number instance, final int defaultValue) {
		return instance != null ? instance.intValue() : defaultValue;
	}

	private static void setText(final JLabel instance, final String text) {
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
						f -> Objects.equals(getName(f), OBJECT_LOCK)), Collectors.toList()),
				x -> get(x, 0), null);
		//
		if (field == null || Narcissus.getField(instance, field) != null) {
			//
			instance.setText(text);
			//
		} // if
			//
	}

	private static <T> Stream<T> stream(final FailableStream<T> instance) {
		return instance != null ? instance.stream() : null;
	}

	private static Class<?>[] getParameterTypes(final Executable instance) throws NoSuchFieldException {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Field field = Narcissus.findField(getClass(instance), "parameterTypes");
		//
		return (field == null || Narcissus.getField(instance, field) != null) ? instance.getParameterTypes() : null;
		//
	}

	private static void forEach(final IntStream instnace, final IntConsumer action) {
		if (instnace != null && Boolean.logicalOr(action != null, Proxy.isProxyClass(getClass(instnace)))) {
			instnace.forEach(action);
		}
	}

	private static void setSelectedItem(final ComboBoxModel<?> instance, final Object item) {
		if (instance != null) {
			instance.setSelectedItem(item);
		}
	}

	private static <E> void insertElementAt(final MutableComboBoxModel<E> instance, final E item, final int index) {
		if (instance != null) {
			instance.insertElementAt(item, index);
		}
	}

	private static int getSize(final ListModel<?> instance) {
		return instance != null ? instance.getSize() : 0;
	}

	private static void testAndRun(final boolean condition, final Runnable runnable) {
		if (condition && runnable != null) {
			runnable.run();
		}
	}

	private static <T> T testAndGet(final boolean condition, final Supplier<T> supplier) {
		return condition && supplier != null ? supplier.get() : null;
	}

	private static boolean endsWith(final String instance, final String suffix) {
		//
		if (instance == null || suffix == null) {
			//
			return false;
			//
		} // if
			//
		final Field field = testAndApply(x -> size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> get(x, 0), null);
		//
		return (field == null || Boolean.logicalAnd(Narcissus.getField(instance, field) != null,
				Narcissus.getField(suffix, field) != null)) && instance.endsWith(suffix);
		//
	}

	private static String group(final MatchResult instance) {
		//
		if (instance == null) {
			//
			return null;
			//
		} // if
			//
		final Field first = testAndApply(x -> size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), "first")), Collectors.toList()),
				x -> get(x, 0), null);
		//
		if (first != null && Objects.equals(first.getType(), Integer.TYPE)) {
			//
			return Narcissus.getIntField(instance, first) >= 0 ? instance.group() : null;
			//
		} // if
			//
		return instance.group();
		//
	}

	private static boolean find(final Matcher instance) {
		//
		if (instance == null) {
			//
			return false;
			//
		} // if
			//
		final Field field = testAndApply(x -> size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), "groups")), Collectors.toList()),
				x -> get(x, 0), null);
		//
		return (field == null || Narcissus.getField(instance, field) != null) && instance.find();
		//
	}

	private static String getName(final RuntimeMXBean instance) {
		return instance != null ? instance.getName() : null;
	}

	private static Matcher matcher(final Pattern instance, final CharSequence input) {
		//
		if (instance == null || input == null) {
			//
			return null;
			//
		} // if
			//
		final Field normalizedPattern = testAndApply(x -> size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), "normalizedPattern")), Collectors.toList()),
				x -> get(x, 0), null);
		//
		final Field value = testAndApply(x -> size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(input), FieldUtils::getAllFieldsList, null)),
						x -> Objects.equals(getName(x), VALUE)), Collectors.toList()),
				x -> get(x, 0), null);
		//
		return (normalizedPattern == null || Narcissus.getField(instance, normalizedPattern) != null)
				&& (value == null || Narcissus.getField(input, value) != null) ? instance.matcher(input) : null;
		//
	}

	private static File getCanonicalFile(final File instance) throws IOException {
		return instance != null && instance.getPath() != null ? instance.getCanonicalFile() : null;
	}

	private static Object getSource(final EventObject instance) {
		return instance != null ? instance.getSource() : null;
	}

	private static void addActionListener(final AbstractButton instance, final ActionListener actionListener) {
		//
		if (instance == null || actionListener == null) {
			//
			return;
			//
		} // if
			//
		final Field field = testAndApply(x -> size(x) == 1,
				collect(filter(
						stream(testAndApply(Objects::nonNull, getClass(instance), FieldUtils::getAllFieldsList, null)),
						f -> Objects.equals(getName(f), "listenerList")), Collectors.toList()),
				x -> get(x, 0), null);
		//
		if (field == null || Narcissus.getField(instance, field) != null) {
			//
			instance.addActionListener(actionListener);
			//
		} // if
			//
	}

	private static String getAbsolutePath(final File instance) {
		return instance != null && instance.getPath() != null ? instance.getAbsolutePath() : null;
	}

	private static void setText(final JTextComponent instance, final String text) {
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
						f -> Objects.equals(getName(f), "model")), Collectors.toList()),
				x -> get(x, 0), null);
		//
		if (field == null || Narcissus.getField(instance, field) != null) {
			//
			instance.setText(text);
			//
		} // if
			//
	}

	private static String getText(final JTextComponent instance) {
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
						f -> Objects.equals(getName(f), "model")), Collectors.toList()),
				x -> get(x, 0), null);
		//
		return field == null || Narcissus.getField(instance, field) != null ? instance.getText() : null;
		//
	}

	private static void setEditable(final JTextComponent instance, final boolean editable) {
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
						f -> Objects.equals(getName(f), OBJECT_LOCK)), Collectors.toList()),
				x -> get(x, 0), null);
		//
		if (field == null || Narcissus.getField(instance, field) != null) {
			//
			instance.setEditable(editable);
			//
		} // if
			//
	}

	private static <T> void forEach(final Iterable<T> instance, final Consumer<? super T> action) {
		if (instance != null && (action != null || Proxy.isProxyClass(getClass(instance)))) {
			instance.forEach(action);
		}
	}

	private static String getName(final Class<?> instance) {
		return instance != null ? instance.getName() : null;
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

	private static void perform(final Document document, final XPath xp, final File f, final String remoteFolderString)
			throws Exception {
		//
		final JFileChooser jfc = new JFileChooser();
		//
		File file = f;
		//
		final boolean isHeadless = GraphicsEnvironment.isHeadless();
		//
		if ((Boolean.logicalOr(!exists(file), !isFile(file))) && !isTestMode() && !isHeadless
				&& jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			//
			file = jfc.getSelectedFile();
			//
		} // if
			//
		String remoteFolder = remoteFolderString;
		//
		final Field field = testAndApply(x -> size(x) == 1,
				collect(filter(stream(testAndApply(Objects::nonNull, getClass(remoteFolderString),
						FieldUtils::getAllFieldsList, null)), x -> Objects.equals(getName(x), VALUE)),
						Collectors.toList()),
				x -> get(x, 0), null);
		//
		final boolean valid = field == null || Narcissus.getField(remoteFolder, field) != null;
		//
		if (remoteFolder == null || (valid && StringUtils.isEmpty(remoteFolder)) || !valid) {
			//
			remoteFolder = Boolean.logicalAnd(!isTestMode(), !isHeadless)
					? JOptionPane.showInputDialog(null, "Remote Folder", remoteFolder)
					: null;
			//
		} // if
			//
		final NodeList nodeList = cast(NodeList.class, evaluate(xp, "/*/host", document, XPathConstants.NODESET));
		//
		Node node = null;
		//
		Map<File, KeyPair> keyPairs = null;
		//
		KeyPair keyPair = null;
		//
		File key = null;
		//
		for (int i = 0; i < getLength(nodeList); i++) {
			//
			if ((node = item(nodeList, i)) == null) {
				//
				continue;
				//
			} // if
				//
			final Node n = node;
			//
			if ((keyPair = get(keyPairs = ObjectUtils.getIfNull(keyPairs, LinkedHashMap::new),
					key = testAndApply(Objects::nonNull, getNodeValue(getNamedItem(getAttributes(node), "key")),
							File::new, null))) == null) {
				//
				put(keyPairs, key,
						keyPair = testAndApply(x -> size(x) == 1,
								testAndApply(x -> Boolean.logicalAnd(exists(x), isFile(x)), key,
										x -> loadKeyPairs(PuttyKeyUtils.DEFAULT_INSTANCE, null, toPath(x), null), null),
								x -> new ArrayList<>(x).get(0), null));
				//
			} // if
				//
			info(LOG, perform(
					testAndApply(Objects::nonNull, getNodeValue(getNamedItem(getAttributes(node), "host")),
							x -> HostAndPort.fromParts(x,
									NumberUtils.toInt(getNodeValue(getNamedItem(getAttributes(n), "port")), 22)),
							null),
					new BasicCredentialsImpl(getNodeValue(getNamedItem(getAttributes(node), "user")),
							getNodeValue(getNamedItem(getAttributes(node), PASSWORD))),
					keyPair, file, remoteFolder));
			//
		} // for
			//
	}

	private static Node item(final NodeList instance, final int index) {
		return instance != null ? instance.item(index) : null;
	}

	private static int getLength(final NodeList instance) {
		return instance != null ? instance.getLength() : 0;
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
						final InputStream is = testAndApply(
								x -> x != null && x.getPath() != null && exists(x) && isFile(x), file,
								FileInputStream::new, null);
						final OutputStream os = Boolean.logicalAnd(isFile(file), remoteFolder != null)
								? write(sftpClient, Objects.toString(remoteFolder))
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
					result.stat = os != null ? stat(sftpClient, Objects.toString(remoteFolder)) : null;
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

	private static <T> void testAndAccept(final Predicate<T> predicate, final T value, final Consumer<T> consumer) {
		if (test(predicate, value)) {
			accept(consumer, value);
		}
	}

	private static <T> void accept(final Consumer<T> instance, final T value) {
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

	private static int length(final int[] instance) {
		return instance != null ? instance.length : 0;
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