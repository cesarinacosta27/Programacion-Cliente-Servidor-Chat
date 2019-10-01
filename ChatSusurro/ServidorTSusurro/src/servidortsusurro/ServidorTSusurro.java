
package servidortsusurro;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 *
 * @author Cesar Acosta
 */
public class ServidorTSusurro {
     
     private static Set<String> names = new HashSet<>();
     
     private static Set<PrintWriter> writers= new HashSet<>();
     
     private static Map<String,PrintWriter> namesWriters= new HashMap<>();
    
    public static void main(String[] args) throws Exception{
        System.out.println("The chat server is running...");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try(ServerSocket listener = new ServerSocket(59001)){
          while(true){
            pool.execute(new Handler(listener.accept()) {});   
          }    
        }
        
    }
    
    private static class Handler implements Runnable{
        private String name;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;
        
        public Handler(Socket socket){
            this.socket=socket;
        }
        
        public void run(){
          try{
              in= new Scanner(socket.getInputStream());
              out= new PrintWriter(socket.getOutputStream(),true);
              
              while(true){
                  out.println("SUBMITNAME");
                  name=in.nextLine();
                  if(name==null){
                      return;
                  }
                  synchronized (name){
                      if(!names.contains(name)){
                          names.add(name);
                          namesWriters.put(name, out);
                          break;
                      }
                  }
              }
              out.println("NAMEACCEPTED "+ name);
              for(PrintWriter writer : writers){
                  writer.println("MESSAGE "+ name +" has joined");
              }
              writers.add(out);
              
              while(true){
                  String input = in.nextLine();
                  if(input.startsWith("/")){
                  if(input.toLowerCase().startsWith("/quit")){
                      return;
                  }
                  if(names.contains(input.substring(1,input.indexOf(" ")))){ 
                         String usuario;
                         String susurro;
                         usuario=input.substring(1,input.indexOf(" "));
                         if(namesWriters.get(usuario).toString().equalsIgnoreCase(namesWriters.get(name).toString())){
                             namesWriters.get(name).println("MESSAGE No te puedes enviar susurros a ti mismo");
                         }else{
                            susurro=input.substring(input.indexOf(" ")+1);
                            namesWriters.get(usuario).println("MESSAGE "+name+" ha susurrado: "+susurro);
                            namesWriters.get(name).println("MESSAGE has susurrado a "+usuario+": "+susurro);                        
                         }
                  }
                  }else{
                   for(PrintWriter writer : writers){
                      writer.println("MESSAGE "+ name + ": "+ input);
                   }
                  }
              }
          }catch(Exception e){
              System.out.println(e);
          }finally{
              if(out!=null){
                  writers.remove(out);
              }
              if(name !=null){
                  System.out.println(name + " is leaving");
                  names.remove(name);
                  for(PrintWriter writer : writers){
                      writer.println("MESSAGE "+ name + " has left");
                  }
              }
              try{socket.close();}catch(IOException e){};
          }  
        }
    }
    
}
