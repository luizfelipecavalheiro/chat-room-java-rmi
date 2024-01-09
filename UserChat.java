import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;

public class UserChat extends UnicastRemoteObject implements IUserChat {
    
    String usrName; //nome do usuario
    
    IServerChat servidor; //referência ao objeto remoto do servidor de chat, usada para chamar os métodos do servidor

    ArrayList<String> roomList; //armazena os nomes das salas disponíveis
    IRoomChat room; //referência ao objeto remoto da sala em que o usuário está atualmente
    Boolean roomIsOpened = true; //indica se a sala está aberta ou fechada

    private JFrame frame; //janela que representa a interface gráfica
    private JTextField textField; //caixa de texto onde o usuário pode digitar mensagens ou nomes de salas
    private JTextArea messageArea; //área de texto onde as mensagens do chat são exibidas

    private JButton sendButton; //permite ao usuário enviar uma mensagem para a sala
    private JButton joinRoomButton; //permite ao usuário entrar em uma sala
    private JButton leaveRoomButton; //permite ao usuário sair da sala atual
    private JButton showRoomButton; //permite ao usuário exibir a lista de salas
    private JButton createRoomButton; //permite ao usuário criar uma nova sala

    private JPanel jPanel; //contém a área de texto das mensagens do chat
    private JScrollPane scrollPane; //envolve a área de texto das mensagens do chat

    public UserChat(String usrName) throws RemoteException{
        //chama construtor da classe mae
    	super();
        this.usrName = usrName;
    }

    private void InitializeGUI(IUserChat user){
        
        //Inicialização da janela:

        frame = new JFrame(usrName);
        frame.setBounds(100, 100, 500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //garantia de close
        frame.getContentPane().setLayout(null);

        //textArea
        
        messageArea = new JTextArea();
        jPanel = new JPanel(new BorderLayout());
        jPanel.setBounds(10, 11, 312, 206);
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(messageArea);
        jPanel.add(scrollPane);
        frame.getContentPane().add(jPanel);

        //input
        textField = new JTextField();
        textField.setBounds(10, 228, 312, 22);
        frame.getContentPane().add(textField);
        textField.setColumns(10);

        //Botao de enviar mensagem
        sendButton = new JButton("Send");
        sendButton.setBounds(335, 228, 89, 23);
        frame.getContentPane().add(sendButton);
        sendButton.addActionListener(new ActionListener() {//button action
            
            public void actionPerformed(ActionEvent e) {
                String msg = textField.getText();
                try {
                    room.sendMsg(usrName, msg);
                    textField.setText("");
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }
        
        });

        //entrar na sala selecionada pelo campo de texto
        joinRoomButton = new JButton("Join");
        joinRoomButton.setBounds(332, 11, 92, 23);
        frame.getContentPane().add(joinRoomButton);

        joinRoomButton.addActionListener(new ActionListener() {//button action
            
            public void actionPerformed(ActionEvent e) {
                String roomName = textField.getText();
                try {
                    roomList = servidor.getRooms();

                    if(roomList.contains(roomName)){
                        String roomUrl = "rmi://localhost:2020/" + roomName;
                        room = (IRoomChat)Naming.lookup(roomUrl);

                        textField.setText("");
                        messageArea.setText("");

                        room.joinRoom(usrName, user); //entra na sala e bloqueia outro join

                        joinRoomButton.setEnabled(false);
                        sendButton.setEnabled(true);
                        leaveRoomButton.setEnabled(true);
                    }

                    else{
                        messageArea.append("Sala inexistente, digite uma sala existente\n");
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        //exit
        leaveRoomButton = new JButton("Leave");
        leaveRoomButton.setBounds(332, 44, 92, 23);
        frame.getContentPane().add(leaveRoomButton);

        leaveRoomButton.addActionListener(new ActionListener() {//button action
            public void actionPerformed(ActionEvent e) {
                try {
                    room.leaveRoom(usrName);
                    messageArea.setText("");
                    sendButton.setEnabled(false);
                    leaveRoomButton.setEnabled(false); //desabilita botão de exit, habilita join
                    joinRoomButton.setEnabled(true);

                    //lista salas
                    try {
                        roomList = servidor.getRooms();
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }

                    String roomListStr = "";
                    for (String room : roomList){
                        roomListStr += '\n' + room;
                    }
                    if(roomListStr.length() == 0)
                        messageArea.append("Servidor vazio!\n");
                    else
                        messageArea.append("Servidor:" + roomListStr + "\n");

                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }
        });
        
        //Criar sala
        createRoomButton = new JButton("Create Room");
        createRoomButton.setBounds(332, 120, 92, 23);
        createRoomButton.addActionListener(new ActionListener() {// button action
            public void actionPerformed(ActionEvent e) {
                String roomName = textField.getText();

                try {
                    servidor.createRoom(roomName);
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }

                textField.setText("");
            }
        });
        frame.getContentPane().add(createRoomButton);

        //botão show salas
        showRoomButton = new JButton("Show Rooms");
        showRoomButton.setBounds(332, 78, 92, 23);
        frame.getContentPane().add(showRoomButton);
        showRoomButton.addActionListener(new ActionListener() {//button action
            public void actionPerformed(ActionEvent e) {

                try {
                    roomList = servidor.getRooms();
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }

                String roomListStr = "";
                for (String room : roomList){
                    roomListStr += '\n' + room;
                }
                if(roomListStr.length() == 0)
                    messageArea.append("Servidor vazio!\n");
                else
                    messageArea.append("Servidor:" + roomListStr + "\n");
            }
        });

        //evita que tu faça ações para a sala estando fora dela
        messageArea.setEditable(false);
        sendButton.setEnabled(false);
        leaveRoomButton.setEnabled(false);
        //habilita entradas na sala
        joinRoomButton.setEnabled(true);
        createRoomButton.setEnabled(true);

        frame.setVisible(true);
    }

    
    public void deliverMsg(String senderName, String msg){
        //concatena remetente com msg
    	String fullMsg = senderName + ": " + msg;

        //Se a mensagem for do servidor fechando a sala
        if(fullMsg.equals("Servidor: Sala fechada pelo servidor.")){
            //limpa a area de texto removendo mensagens
        	messageArea.setText("");
        	//adiciona a msg  a area de texto
            messageArea.append(fullMsg + "\n");
            //desabilita botao de envio de msg
            sendButton.setEnabled(false);
            //desabilita botao de saida da sala
            leaveRoomButton.setEnabled(false);
            //habilita botao de entrada na sala
            joinRoomButton.setEnabled(true);

            String roomListStr = "";

            try {
            	//obtem lista atualizada de salas
                roomList = servidor.getRooms();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            for (String room : roomList){
                roomListStr += ' ' + room;
            }
            if(roomListStr.length() == 0)
            	//se o tamanho for zero, nao tem salas no servidor
                messageArea.append("Nao ha salas!\n");
            else
            	//printa salas
                messageArea.append("Servidor:" + roomListStr + "\n");
            
        //se nao for, adiciona a mensagem na area de mensagem
        }else
            messageArea.append(fullMsg + "\n");
        return;
    }

    public static void main(String[] args) {
    	
    	if(args.length != 1){
            System.out.println("Adicione nome como parametro");
            return;
        }else if (args[0].equals("Servidor")){
            System.out.println("Nome invalido, escolha outro nome");
            return;
        }

    	
        try {
        	//cria instancia passando como parametro o nome de usuario digitado
            UserChat user = new UserChat(args[0]);

            user.InitializeGUI(user);

            //busca do objeto remoto IServerChat com o nome informado
            //estabelece conexão com o servidor de chat remoto
            user.servidor = (IServerChat)Naming.lookup("rmi://localhost:2020/Servidor");

            //lista de salas disponiveis
            ArrayList<String> roomList = user.servidor.getRooms();

            String roomListStr = "";
            for (String room : roomList){
            	//concatena salas na string
                roomListStr += '\n' + room;
            }
            if(roomListStr.length() == 0)
            	//caso nao tenha salas
                user.messageArea.append("Servidor vazio!\n");
            else
            	//caso tenha salas
                user.messageArea.append("Servidor:" + roomListStr + "\n");

        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
