
package servidortbloqueopersistencia;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 *
 * @author Cesar Acosta
 */
public class ServidorTBloqueoPersistencia {
     
     private static Set<String> names = new HashSet<>();
     
     private static Set<PrintWriter> writers= new HashSet<>();
     
     private static Map<String,PrintWriter> namesWriters= new HashMap<>();
     
     private static Map<String,Set> namesBlocked= null;
    
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
                if(namesBlocked==null){
                try{
                    String link=trimLink(); 
                    FileInputStream fis=new FileInputStream(link+"blockedList.conf");
                    ObjectInputStream ois=new ObjectInputStream(fis);
                    namesBlocked=(HashMap) ois.readObject();
                    System.out.println("Lista de bloqueados cargada");
                    ois.close();
                    fis.close();
                  }catch(IOException e){
                     if(e.toString().startsWith("java.io.FileNotFoundException")){
                       namesBlocked= new HashMap<>();
                     }else{e.printStackTrace();}
                  }
                }
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
              imprimirBlqueados(name, "has joined");
              writers.add(out);
              
              while(true){
                  String input = in.nextLine();
                  if(input.startsWith("/")){
                    if(input.toLowerCase().startsWith("/quit")){
                        return;
                    }
                    if(input.indexOf(" ")>=0){
                    if(names.contains(input.substring(1,input.indexOf(" ")))){ 
                           String usuario;
                           String susurro;
                           usuario=input.substring(1,input.indexOf(" "));
                           Set bloqueados=namesBlocked.get(name);
                           
                            
                           if(namesWriters.get(usuario).toString().equalsIgnoreCase(namesWriters.get(name).toString())){
                               namesWriters.get(name).println("MESSAGE No te puedes enviar susurros a ti mismo");
                           }else{
                              susurro=input.substring(input.indexOf(" ")+1);
                              if(bloqueados!=null){
                              if(!bloqueados.contains(usuario)){
                                namesWriters.get(usuario).println("MESSAGE "+name+" ha susurrado: "+susurro);
                                namesWriters.get(name).println("MESSAGE has susurrado a "+usuario+": "+susurro);                        
                              }else if(bloqueados.contains(usuario)){
                                namesWriters.get(name).println("MESSAGE has susurrado a "+usuario+": "+susurro);  
                              }
                           }else{
                                namesWriters.get(usuario).println("MESSAGE "+name+" ha susurrado: "+susurro);
                                namesWriters.get(name).println("MESSAGE has susurrado a "+usuario+": "+susurro);                          
                              }
                         }
                    }
                    }
                    if(input.toLowerCase().startsWith("/block")){
                      if(input.indexOf(" ")>=0){
                        Set<String> bloqueadores;
                        String bloqueado=input.substring(input.indexOf(" ")+1);
                        if(namesBlocked.get(bloqueado)!=null){
                            bloqueadores=namesBlocked.get(bloqueado);
                        }else{
                            bloqueadores=new HashSet<>();
                            namesBlocked.put(bloqueado, bloqueadores);
                        }
                        bloqueadores.add(name);
                        namesBlocked.replace(bloqueado, bloqueadores);
                      }
                    }
                    if(input.toLowerCase().startsWith("/unblock")){
                      if(input.indexOf(" ")>=0){
                        Set<String> bloqueadores;
                        String bloqueado=input.substring(input.indexOf(" ")+1);
                        if(namesBlocked.get(bloqueado)!=null){
                            bloqueadores=namesBlocked.get(bloqueado);
                        }else{
                            bloqueadores=new HashSet<>();
                            namesBlocked.put(bloqueado, bloqueadores);
                        }
                        bloqueadores.remove(name);
                        if(!bloqueadores.isEmpty()){
                         namesBlocked.replace(bloqueado, bloqueadores);
                        }else{
                            namesBlocked.remove(bloqueado);
                        }
                      }
                    }
                  }else{
                      imprimirBlqueados(name, input);                  
                  }
              }
          }catch(Exception e){
              e.printStackTrace();
          }finally{
              if(out!=null){
                  writers.remove(out);
              }
              if(name !=null){
                  System.out.println(name + " is leaving");
                  names.remove(name);
                  imprimirBlqueados(name, "has left");
              }
              guardarMap();
              try{socket.close();}catch(IOException e){e.printStackTrace();};
          }  
        }
    }
   
    public String getLink(){
      URL link=this.getClass().getProtectionDomain().getCodeSource().getLocation(); 
      return link.toString();
    }
    
    public static String trimLink(){
        String linkEntero="";
                    try {
                       ServidorTBloqueoPersistencia clase= new ServidorTBloqueoPersistencia();
                       linkEntero=clase.getLink();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
               
                int endIndex= linkEntero.length()-37;
                String link=linkEntero.substring(6,endIndex);
                link=link.replaceAll("%20", " ");   
           return link;
    }
    
    public static void imprimirBlqueados(String name,String input){
        Set setP= namesWriters.entrySet();
        Iterator iteratorP=setP.iterator();
        Set bloqueadores=null;
        if(namesBlocked.get(name)!=null){
            bloqueadores=namesBlocked.get(name);
        }
        while(iteratorP.hasNext()){
           Map.Entry nWEntry=(Map.Entry)iteratorP.next();
           if(bloqueadores!=null){
           if(!bloqueadores.contains(nWEntry.getKey())){
            PrintWriter writer=(PrintWriter)nWEntry.getValue();
            writer.println("MESSAGE "+ name + ": "+ input);
           }
           }else{
            PrintWriter writer=(PrintWriter)nWEntry.getValue();
            writer.println("MESSAGE "+ name + ": "+ input);    
           }
        }
    }
    
    public static void guardarMap(){
              try {
                String link=trimLink();  
                FileOutputStream fos=new FileOutputStream(link+"blockedList.conf");
                ObjectOutputStream oos=new ObjectOutputStream(fos);    
                oos.writeObject(namesBlocked);
                oos.close();
                fos.close();
              } catch (Exception e) {
                  e.printStackTrace();
              }
    }
     
}
