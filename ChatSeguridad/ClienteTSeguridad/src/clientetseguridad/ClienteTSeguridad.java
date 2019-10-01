

package clientetseguridad;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 *
 * @author Cesar Acosta
 */
public class ClienteTSeguridad {

   String serverAddress;
   Scanner in;
   PrintWriter out;
   JFrame frame = new JFrame("Chatter");
   JTextField textField = new JTextField(50);
   JTextArea messageArea = new JTextArea(16,50);
   
   public ClienteTSeguridad(String serverAddress){
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
                 }else if(line.startsWith("SUBMITPASSWORD")){
                     out.println(getPassword());   
                 }else if(line.startsWith("NAMEACCEPTED ")){
                   this.frame.setTitle("Chatter - "+line.substring(13));
                   textField.setEditable(true);
                 }else if(line.startsWith("MESSAGE ")){
                     messageArea.append(line.substring(8)+"\n");
                 }else if(line.startsWith("WRONGPASSWORD")){
                     out.println(wrongPassword());
                    
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
         "Choose your screen name: ",
         "Screen name selection",
         JOptionPane.PLAIN_MESSAGE
         );
       }
     private String getPassword(){
         String pass="";
         Box caja = Box.createVerticalBox();
         JLabel j1= new JLabel("Password: ");
         JPanel jlp= new JPanel();
         jlp.setLayout(new BoxLayout(jlp,BoxLayout.X_AXIS));
         jlp.add(j1);
         jlp.add(Box.createHorizontalGlue());
         caja.add(jlp);
         
         JPasswordField jpf=new JPasswordField(4);
         jpf.requestFocusInWindow();
         caja.add(jpf);
         jpf.addAncestorListener(new AncestorListener() {
             @Override
             public void ancestorAdded(AncestorEvent event) {
                 event.getComponent().requestFocusInWindow();
             }

             @Override
             public void ancestorRemoved(AncestorEvent event) {
             }

             @Override
             public void ancestorMoved(AncestorEvent event) {
             } 
         }); 
         int opc=JOptionPane.showConfirmDialog(frame,
         caja,
         "Password selection",
         JOptionPane.OK_CANCEL_OPTION,
         JOptionPane.INFORMATION_MESSAGE);
         if(opc==0){
           char[] contra = jpf.getPassword();
           pass= new String(contra);
         }
         if(opc==2 || opc==-1){
             pass="null";
         }
         return pass;
       }
     public int wrongPassword(){
         
         return JOptionPane.showConfirmDialog(
         frame, 
         "La contraseña ingresada no coincide", 
         "Contraseña Incorrecta",
         JOptionPane.PLAIN_MESSAGE,
         JOptionPane.ERROR_MESSAGE);
     }
    public static void main(String[] args) throws Exception {
         if(args.length!= 1){
           System.err.println("Pass the server ip as the source argument");
           return;
         }
      ClienteTSeguridad client= new ClienteTSeguridad(args[0]);
       client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       client.frame.setVisible(true);
       client.run();
    }
    
}
