
package clientet;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Cesar Acosta
 */
public class ClienteT {

   String serverAddress;
   Scanner in;
   PrintWriter out;
   JFrame frame = new JFrame("Chatter");
   JTextField textField = new JTextField(50);
   JTextArea messageArea = new JTextArea(16,50);
   
   public ClienteT(String serverAddress){
       this.serverAddress = serverAddress; 
        
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField,BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea),BorderLayout.CENTER);
        frame.pack();
        
        textField.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e){
              out.println(textField.getText());
              textField.setText("");
           }
        });
   }
         private void run() throws IOException{
          try{
            Socket socket = new Socket(serverAddress,59001);
            in= new Scanner(socket.getInputStream());
            out= new PrintWriter(socket.getOutputStream(),true);
             while(in.hasNextLine()){
                 String line=in.nextLine();
                 if(line.startsWith("SUBMITNAME")){
                     out.println(getName());
                 }else if(line.startsWith("NAMEACCEPTED ")){
                   this.frame.setTitle("Chatter - "+line.substring(13));
                   textField.setEditable(true);
                 }else if(line.startsWith("MESSAGE ")){
                     messageArea.append(line.substring(8)+"\n");
                 }
             }
            }finally{
                  frame.setVisible(false);
                  frame.dispose();
           }
       }
   
    private String getName(){
         return JOptionPane.showInputDialog(
         frame,
         "Choose a screen name: ",
         "Screen name selection",
         JOptionPane.PLAIN_MESSAGE
         );
       }
    public static void main(String[] args) throws Exception {
         if(args.length!= 1){
           System.err.println("Pass the server ip as the source argument");
           return;
         }
      ClienteT client= new ClienteT(args[0]);
       client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       client.frame.setVisible(true);
       client.run();
    }
    
}
