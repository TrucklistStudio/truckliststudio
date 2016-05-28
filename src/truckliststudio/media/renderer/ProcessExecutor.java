/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package truckliststudio.media.renderer;

import com.sun.jna.Pointer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import static truckliststudio.TrucklistStudio.os;
import truckliststudio.externals.ProcessRenderer;
import truckliststudio.util.Tools;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

/**
 *
 * @author patrick (modified by karl)
 */
public class ProcessExecutor {
    private static String childPids = "";
    private static final String userHomeDir = Tools.getUserHome();
    private static final Runtime rt = Runtime.getRuntime();

    public static int getPID(Process process) throws Exception //Author Martijn Courteaux Code
    {
//    System.out.println("Process_GetUnixPid: "+process.getClass().getName());
    String pName = process.getClass().getName();
//    System.out.println("ProcessType="+pName);
    if (pName.equals("java.lang.UNIXProcess")) {
        Class cl = process.getClass();
        Field field = cl.getDeclaredField("pid");
        field.setAccessible(true);
        Object pidObject = field.get(process);
        return (Integer) pidObject;
    } else if (os.equals(os.WINDOWS)){
//        System.out.println("Windows Process ...");
        int ret = 0;
        try {
            Field f = process.getClass().getDeclaredField("handle");
            f.setAccessible(true);
            long handl = f.getLong(process);

            Kernel32 kernel = Kernel32.INSTANCE;
            WinNT.HANDLE handle = new WinNT.HANDLE();
            handle.setPointer(Pointer.createConstant(handl));
            ret = kernel.GetProcessId(handle);
//            System.out.println("Detected Win32 pid: "+ ret);
            
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return (ret);
    } else {
        throw new IllegalArgumentException("Needs to be a UNIX or Win32 Process");
    }
    }

    public static void killUnixProcess(Process process) throws Exception //Modified from Martijn Courteaux Code
    {
    int pid = getPID(process);
//    System.out.println("Process_Pid: "+pid);
    String commandPids = "ps -ef | awk '{if ($3 == "+pid+") print $2;}'";
    File fileP=new File(userHomeDir+"/.webcamstudio/"+"WSBust.sh");
    FileOutputStream fosV;
    Writer dosV = null;
    try {
        fosV = new FileOutputStream(fileP);
        dosV= new OutputStreamWriter(fosV);
    } catch (FileNotFoundException ex) {
        Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
    }
    try {
        dosV.write("#!/bin/bash\n");
        dosV.write(commandPids+"\n");
        dosV.close();
    } catch (IOException ex) {
        Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
    }
    String batchPidCommand = userHomeDir+"/.webcamstudio/"+"WSBust.sh";
    fileP.setExecutable(true);
    Tools.sleep(10);
    try {
        Process getChildPids = rt.exec(batchPidCommand);
        Tools.sleep(10);
        getChildPids.waitFor(); //Author spoonybard896
        BufferedReader buf = new BufferedReader(new InputStreamReader(
                getChildPids.getInputStream()));
        String line = "";
        childPids = "";
        while ((line = buf.readLine()) != null) {
            childPids += line + "\n";
        } //Author spoonybard896
    } catch (IOException | InterruptedException e) {
        e.printStackTrace();
    }
    rt.exec("kill " + pid).waitFor(); // andrew.silver0 mod from -9
    rt.exec("kill " + childPids).waitFor(); //andrew.silver0 mod from -9
//    System.out.println("ChildPid: "+childPids);
    childPids = null;
    }

    public static void killWin32Process(Process process) throws Exception //Modified from Martijn Courteaux Code
    {
        int pid = getPID(process);
//        System.out.println("Process_Pid: "+pid);
        String batchPidCommand = "taskkill /F /pid " + pid;
        rt.exec(batchPidCommand);
        Tools.sleep(10);
    }

    private Process process;
    private boolean processRunning = false;
    private String name = "";

    public ProcessExecutor(String gName) {
        this.name = gName;
    }

    private void readOutput(final Process p){
        new Thread(new Runnable() {

            @Override
            public void run() {
                InputStream in1 = p.getErrorStream();
                InputStream in2 = p.getInputStream();
                byte[] buffer = new byte[65536];
                int count = 0;
                while (count != -1 && processRunning){
                    try {
                        count = in1.read(buffer);
                        if (count > 0){
                            System.out.println("Process Err: " + new String(buffer,0,count));
                        }
//                        count = in2.read(buffer);
//                        if (count > 0){
//                            System.out.println("FFMPEG Out: " + new String(buffer,0,count));
//                        }
                        Tools.sleep(100);
                        
                    } catch (IOException ex) {
                        Logger.getLogger(ProcessExecutor.class.getName()).log(Level.SEVERE, null, ex);
                        break;
                    }
                    
                }
                System.out.println("Process Closing streams");
                
            }
        }).start();
    }
    
    public void execute(String[] params) throws IOException, InterruptedException {
        process = rt.exec(params);
        processRunning = true;
//        readOutput(process);      
    }
    
    // Testing console capture
    public Process executeC(String[] params) throws IOException, InterruptedException {
        process = rt.exec(params);
        processRunning = true;
        return process;
//        readOutput(process);      
    }
    
    public void executeString(String params) throws IOException, InterruptedException {
        
        process = rt.exec(params);
//        System.out.println("Process: "+process);
        processRunning = true;
//        readOutput(process);
    }
    
    public void destroy() {
        processRunning=false;
        try {
            if (os.equals(os.WINDOWS)){
                killWin32Process(process);
            } else {
                killUnixProcess(process);
            }
        } catch (Exception ex) {
            Logger.getLogger(ProcessExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getProcessPID(){
        try {
            if (process != null){
                int parentPID = getPID(process);
                String commandPids = "ps -ef | awk '{if ($3 == "+parentPID+") print $2;}'";
                File fileP=new File(userHomeDir+"/.webcamstudio/"+"WSBust.sh"); 
                FileOutputStream fosV;
                Writer dosV = null;
                try {
                    fosV = new FileOutputStream(fileP);
                    dosV= new OutputStreamWriter(fosV);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    dosV.write("#!/bin/bash\n");
                    dosV.write(commandPids+"\n");
                    dosV.close();
                } catch (IOException ex) {
                    Logger.getLogger(ProcessRenderer.class.getName()).log(Level.SEVERE, null, ex);
                }
                fileP.setExecutable(true);
                String batchPidCommand = userHomeDir+"/.webcamstudio/"+"WSBust.sh";
                try {
                    Process getChildPids = rt.exec(batchPidCommand);
                    getChildPids.waitFor(); //Author spoonybard896
                    BufferedReader buf = new BufferedReader(new InputStreamReader(
                    getChildPids.getInputStream()));
                    String line = "";
                    childPids = "";
                    while ((line = buf.readLine()) != null) {
                        childPids += line + "\n";
                    } //Author spoonybard896
                } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                }
            }   
        } catch (Exception ex) {
            Logger.getLogger(ProcessExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return childPids;
    }
}
