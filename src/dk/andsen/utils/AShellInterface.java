/**
 * Part of aShell (http://aaa.andsen.dk/)
 * A SQLite Manager by Andsen
 *
 * This class contains the interface used to execute several commands
 * the shell is kept open until closed
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
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import dk.andsen.asqlitemanager.Prefs;
import dk.andsen.types.ShellOutputHolder;
import dk.andsen.utils.Utils;

public class AShellInterface {
	//for shell sessions shell kept open until close
	private String _shell = "sh";
  private String EXIT = "exit\n";
  private String TAG = "root";
  private DataOutputStream os = null;
  private Process process = null;
  private InputStreamHandler shOut;
  private InputStreamHandler shErr;
  private ArrayList<ShellOutputHolder> intOuts = new ArrayList<ShellOutputHolder>();
  private ArrayList<ShellOutputHolder> intErrs = new ArrayList<ShellOutputHolder>();
  private ArrayList<ShellOutputHolder> intCmds = new ArrayList<ShellOutputHolder>();
	public int noOfCommands;
	// minimum number of loops to do before returning StdOut 
	private int _minDelay = 30;
	private int _maxDelay = 200;
	private int lengthOfStdOut;
	private int lengthOfStdErr;
	private boolean _log;

  private enum OUTPUT {
    STDERR,
    STDOUT
  }
  
	public AShellInterface(String shell, int minDelay, Context cont) {
		_minDelay = minDelay;
		_shell = shell;
		_log = Prefs.getLogging(cont);

    try {
			process = Runtime.getRuntime().exec(_shell);
		  os = new DataOutputStream(process.getOutputStream());
		  shOut = sinkProcessOutput(process, OUTPUT.STDOUT);
		  shErr = sinkProcessOutput(process, OUTPUT.STDERR);
		  noOfCommands = 0;
		  lengthOfStdOut = 0;
		  lengthOfStdErr = 0;
		} catch (IOException ignored) {
		}
	}
	
  /**
   * Holds all output from STDOUT for the session
   * @return
   */
  public ArrayList<String> getStdOut() {
		ArrayList<String> strs = new ArrayList<String>(); 
		for(ShellOutputHolder line: intOuts) {
			if (line.getCommandNo() == noOfCommands) {
				String str = line.getLine();
				strs.add(str);
			}
		}
  	return strs;
	}

	/**
	 * Holds all output from STDERR for the session
	 * @return a String[]
	 */
	public ArrayList<String> getStdErr() {
		ArrayList<String> strs = new ArrayList<String>(); 
		for(ShellOutputHolder line: intErrs) {
			if (line.getCommandNo() == noOfCommands) {
				String str = line.getLine();
				// this handles the blank line after first call th shell
				if(!str.trim().equals(""))
					strs.add(str);
				else
					lengthOfStdErr--;
			}
		}
  	return strs;
	}
	
	/**
	 * Return the number of the current command
	 * @return
	 */
	public int getNoOfCommands() {
		return noOfCommands;
	}

  /**
   * Execute a command in the current shell session
   * @param command
   */
  public void runCommand(String command) {
    try {
			noOfCommands++;
			os.writeBytes("echo aShellFrom \n");
			os.flush();
			os.writeBytes(command + '\n');
			ShellOutputHolder cmd = new ShellOutputHolder();
    	// placing current command in output stack
			cmd.setCommandNo(noOfCommands);
			cmd.setLine(command);
			intCmds.add(cmd);
			os.flush();
			os.writeBytes("echo aShellTo \n");
			os.flush();
    	String out = shOut.getOutput();
    	String err = shErr.getOutput();
    	int count = 0;
    	int start = 0;
    	int length = 0;
    	String[] outs = null;
    	String[] errs = null;
    	// Make sure process has written everything to stdout
    	
    	for (count = 0; count < _minDelay; count++) {
    		outs = shOut.getOutput().split("\n");
    	}
    	while (!out.endsWith("aShellTo\n") && (count < _maxDelay)) {  
    		count++;
    		length = shOut.getOutput().length();
    		start = 0;
    		if (length > 10)
    			start = length - 10;
    		Utils.logD(count + " -" + shOut.getOutput().substring(start, length) + "-", _log);
    		out = shOut.getOutput();
    	}
    	// after first call a string with new line returned
    	err = shErr.getOutput();
    	outs = out.split("\n");
    	int lengthOut = out.split("\n").length;
			Utils.logD("lengthOut " + lengthOut + " lengthOfStdOut " + lengthOfStdOut, _log);
    	int lengthErr = err.split("\n").length;
    	// New output?
    	if (lengthOut > lengthOfStdOut) {
    		Utils.logD("Show output", _log);
      	for (int i = lengthOut-2; i > lengthOfStdOut +0; i--) {
      		cmd = new ShellOutputHolder();
    			cmd.setCommandNo(noOfCommands);
    			cmd.setLine(outs[i]);
    			Utils.logD("OUT" + outs[i], _log);
      		intOuts.add(cmd);
      	}
    	}
    	errs = err.split("\n");
    	// new error messages?
			Log.d(TAG, "lengthErr " + lengthErr + " lengthOfStdErr " + lengthOfStdErr);
    	if (lengthErr > lengthOfStdErr) {
    		Utils.logD("Error!", _log);
      	for (int i = lengthErr -1; i >= lengthOfStdErr +0; i--) {
      		cmd = new ShellOutputHolder();
      		cmd.setCommandNo(noOfCommands);
    			cmd.setLine(errs[i]);
    			Utils.logD("ERR" + errs[i], _log);
      		intErrs.add(cmd);
      	}
    	}
    	// placing current commands output in output stack
    	lengthOfStdOut = lengthOut;
    	lengthOfStdErr = lengthErr;
    	Utils.logD("lengthOfStdOut " + lengthOfStdOut, _log); 
		} catch (Exception e) {
			Utils.logE("e " + e.toString(), true); //TODO configurable
			e.printStackTrace();
		}
  }
  
  private InputStreamHandler sinkProcessOutput(Process p, OUTPUT o) {
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
	  	os.writeBytes(EXIT);
			os.flush();
			os.close();
			process.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}  

  /**
   * @author mh
   *
   */
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
