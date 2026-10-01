# sftp-upload-batch

## Command

### Single Host
<pre>java -jar sftp-upload-batch-0.0.1-SNAPSHOT.jar host=127.0.0.1 user=user password=password remoteFolder=//tmp file=pom.xml</pre>

## Configuration File (Multiple Host)
<pre>java -jar sftp-upload-batch-0.0.1-SNAPSHOT.jar config=sftp-upload-batch.xml               remoteFolder=//tmp file=pom.xml</pre>

### sftp-upload-batch.xml
<pre>
&lt;config&gt;
	&lt;host host=&quot;127.0.0.1&quot; user=&quot;user&quot; password=&quot;password&quot;/&gt;
&lt;/config&gt;
</pre>

## Gui

The below command force the program start in <b>gui</b> mode.
<pre>java -jar sftp-upload-batch-0.0.1-SNAPSHOT.jar gui=true</pre>

Under Microsoft Windows or Apple MacOSX, double the jar to start the program under <b>gui</b> mode.
