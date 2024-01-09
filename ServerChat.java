import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;

public class ServerChat extends UnicastRemoteObject implements IServerChat {
	private ArrayList<String> roomList; //armazena os nomes das salas disponíveis
	
	private JFrame frame; //representa a janela da interface gráfica do servidor
    private JButton ButtonCloseRoom; //botao que permite fechar a salas selecionada
    private JList<String> roomListArea; //exibe a lista de salas disponíveis 
    private JScrollPane listScroller; //permite a rolagem da lista de salas
    private JPanel jPanel; //painel que contém a lista de salas
	
    
    private void ServerGUI(){
        //Cria o frame
        frame = new JFrame("Servidor");
        frame.setBounds(120, 120, 290, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        //Botao para fechar a sala
        ButtonCloseRoom = new JButton("Close Room");
        ButtonCloseRoom.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> roomsToBeClosed = new ArrayList<String>(roomListArea.getSelectedValuesList());

                for (String roomName : roomsToBeClosed){
                    String roomUrl = "rmi://localhost:2020/" + roomName;

                    roomList.remove(roomName);

                    try {
                        IRoomChat room = (IRoomChat)Naming.lookup(roomUrl);
                        room.closeRoom();
                    } catch (MalformedURLException | RemoteException | NotBoundException e1) {
                        e1.printStackTrace();
                    }
                }

                String[] roomListStr = roomList.toArray(new String[0]);
                roomListArea.setListData(roomListStr);

            }
        });
        ButtonCloseRoom.setBounds(10, 120, 120, 23);
        frame.getContentPane().add(ButtonCloseRoom);

        //Lista com as salas disponíveis
        roomListArea = new JList<String>();
        jPanel = new JPanel(new BorderLayout());
        jPanel.setBounds(10, 10, 120, 100);
        listScroller = new JScrollPane();
        listScroller.setViewportView(roomListArea);
        roomListArea.setLayoutOrientation(JList.VERTICAL);
        jPanel.add(listScroller);
        frame.getContentPane().add(jPanel);

        frame.setVisible(true);
    }

    
    
	public ServerChat() throws RemoteException {
		//chama o construtor da classe mãe p/ configurar a exportação do obj 
		super();
		//cria instancia que será a roomList
		roomList = new ArrayList<String>();
		//chama interafce grafica
		ServerGUI();
	}
	
	
	@Override
	public ArrayList<String> getRooms() {
		//retorna lista de salas
		return this.roomList;
	}

	@Override
	public void createRoom(String roomName) throws RemoteException {
		//cria URL da sala
		String roomUrl = "rmi://localhost:2020/" + roomName;
        //cria instancia de RoomChat
		RoomChat roomChat = new RoomChat(roomName);

        //se a sala ainda nao existe e o nome for valido
        if(!roomList.contains(roomName) && !roomName.equals("")){
            try {
            	//vincua objeto remoto roomChat a um nome roomUrl no registro RMI
            	//assim o cliente pode acessar o obj remoto, fazendo chamada ao
            	//registro rmi utilizando o nome
                Naming.rebind(roomUrl, roomChat);
                //Adiciona sala na roomList
                roomList.add(roomName);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        //transforma ArrayList para String[] para poder mostrar na JList
        String[] roomListStr = roomList.toArray(new String[0]);
        //atualiza a exibicao da lista na interface
        roomListArea.setListData(roomListStr);

	}
	

	public static void main(String args[]) {
		
		try {
			//cria instancia de serverChat que por sua vez chama a interface (que é configurada)
			ServerChat servidor = new ServerChat();
			//cria registro rmi na porta 2020
			LocateRegistry.createRegistry(2020);
			//registra o servidor, vinculando a um nome (URL)
			Naming.rebind("rmi://localhost:2020/Servidor", servidor);
			System.out.println("Server is running...");
			
		}catch(Exception e) {
			System.out.println("Error: " + e);
		}
	}
	
}

