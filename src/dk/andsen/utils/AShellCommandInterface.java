/**
 * Part of aShell (http://aaa.andsen.dk/)
 * A SQLite Manager by Andsen
 *
 * This class contains the interface used to execute single commands 
 *
 * @author Andsen
 *
 */

package dk.andsen.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;

import dk.andsen.asqlitemanager.Prefs;
import dk.andsen.utils.Utils;

public class AShellCommandInterface {
	private String _shell = "sh";
	private String _shell2 = "sh";
  private String EXIT = "exit\n";
  //private String TAG = "root";
  private DataOutputStream os = null;
  private Process process = null;
  private Process process2 = null;
  private InputStreamHandler shOut;
  private InputStreamHandler shErr;
	public int noOfCommands;
	// minimum number of loops to do before returning StdOut 
	private int _min = 30;
	private int _max = 100;
	private boolean _log;

  private enum OUTPUT {
    STDERR,
    STDOUT
  }
  



	public AShellCommandInterface(String shell,Context cont) {
		_shell = shell;
		_log = Prefs.getLogging(cont);

    try {
			process2 = Runtime.getRuntime().exec(_shell2);
		  os = new DataOutputStream(process2.getOutputStream());
		  shOut = sinkProcessOutput(process2, OUTPUT.STDOUT);
		  shErr = sinkProcessOutput(process2, OUTPUT.STDERR);
		  noOfCommands = 0;
		} catch (IOException ignored) {
		}
	}
	
  /**
   * Holds all output from STDOUT for the session
   * @return
   */
  public String[] getStdOut() {
  	String[] outs = null;
    if (shOut != null) {
    	// out holds all of the sessions output
    	String out = shOut.getOutput();
    	int count = 0;
    	int start = 0;
    	int length = 0;
    	for (count = 0; count < _min; count++) {
    		outs = shOut.getOutput().split("\n");
    	}
    	while (!out.endsWith("aShellTo\n") && (count < _max)) {  
    		count++;
    		length = shOut.getOutput().length();
    		start = 0;
    		if (length > 10)
    			start = length - 10;
    		Utils.logD(count + " -" + shOut.getOutput().substring(start, length) + "-", _log);
    		out = shOut.getOutput();
    	}
    	out = out + count + "\n";
    	outs = out.split("\n");
    	for (int i = 0; i < outs.length; i++) {
    		Utils.logD("OUT " + outs[i], _log);
    	}
    }
		return outs;
	}

	/**
	 * Holds all output from STDERR for the session
	 * @return a String[]
	 */
	public String[] getStdErr() {
		String[] errs = null;
    if (shErr != null) {
    	// err holds all of the sessions output
    	errs = shErr.getOutput().split("\n");
    	for (int i = 0; i < errs.length; i++) {
    		Utils.logD("ERR " + errs[i], _log);
    	}
    }
    return errs;
	}
	
  public boolean isRootAvailable() {
    String out = null;
    _shell = "su";
    out = runCommandOut("ls /data");
    if (out != null && out.length() > 0)
    	return true;
    else
    	return false;
  }

  public String runCommandOut(String command) {
    try {
      return _runCommand(command, OUTPUT.STDOUT);
    } catch (IOException ignored) {
      return null;
    }
  }

  public String runCommandErr(String command) {
    try {
      return _runCommand(command, OUTPUT.STDERR);
    } catch (IOException ignored) {
      return null;
    }
  }

  private String _runCommand(String command, OUTPUT o) throws IOException {
    try {
    	//Utils.logD(command);
      process = Runtime.getRuntime().exec(_shell);
      DataOutputStream os = new DataOutputStream(process.getOutputStream());
      InputStreamHandler sh = sinkProcessOutput(process, o);
      os.writeBytes(command + '\n');
      os.flush();
      os.writeBytes(EXIT);
      os.flush();
      process.waitFor();
      if (sh != null) {
        String output = sh.getOutput();
        return output;
      } else {
        return null;
      }
    } catch (Exception e) {
      final String msg = e.getMessage();
      Utils.logD("runCommand error: " + msg + e.toString(), _log);
      throw new IOException(msg);
    } finally {
      try {
        if (os != null) {
          os.close();
        }
        if (process != null) {
          process.destroy();
        }
      } catch (Exception ignored) {}
    }
  }

  public InputStreamHandler sinkProcessOutput(Process p, OUTPUT o) {
    InputStreamHandler output = null;
    //InputStreamHandler output = null;
    switch (o) {
      case STDOUT:
        output = new InputStreamHandler(p.getInputStream(), false);
        break;
      case STDERR:
        output = new InputStreamHandler(p.getErrorStream(), false);
        break;
    }
    return output;
  }

  /**
	 * Close current shell session
	 */
	public void close() {
	  try {
			os.close();
			process2.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}  

  private static class InputStreamHandler extends Thread {
    private final InputStream stream;
    private final boolean sink;
    StringBuffer output;
    public String getOutput() {
      return output.toString();
    }

    InputStreamHandler(InputStream stream, boolean sink) {
      this.sink = sink;
      this.stream = stream;
      start();
    }

    @Override
    public void run() {
      try {
        if (sink) {
          while (stream.read() != -1) {}
        } else {
          output = new StringBuffer();
          BufferedReader b = new BufferedReader(new InputStreamReader(stream));
          String s;
          while ((s = b.readLine()) != null) {
            output.append(s + "\n");
          }
        }
      } catch (IOException ignored) {}
    }
  }
}