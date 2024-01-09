import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

//estende UnicastRemoteObject para habilitar a comunicação remota através do RMI.
public class RoomChat extends UnicastRemoteObject implements IRoomChat {
    private Map<String, IUserChat> userList;
    private String roomName;

    public RoomChat(String roomName) throws RemoteException{
        super();
        userList = new HashMap<String, IUserChat>();
        this.roomName = roomName;
    }

    //recebe o remetente e a mensagem em si
    public void sendMsg(String usrName, String msg) throws RemoteException{
        //percorre o map invocando o metodo deliverMsg em cada obj remoto IUserChat
    	//O Map é separado por <K, V>, sendo a chave o nome do usuario na sala e valor a instancia de IUserChat
    	for (Map.Entry<String, IUserChat> user : userList.entrySet()){
            
    		//atual usuario nao é o remetente, vai aparecer o usrName na tela
    		if(!user.getKey().equals(usrName))
                user.getValue().deliverMsg(usrName, msg);
    		//atual usuario é o remetente, vai aparecer o "Voce" na tela
            else
                user.getValue().deliverMsg("Voce", msg);
        }
        return;
    }

    //recebe o usrName e o obj correspondente ao usuário
    public void joinRoom(String usrName, IUserChat user) throws RemoteException{
        //adiciona na lista (sala)
    	userList.put(usrName, user);
    	//chama o metodo sendMsg para informar sobre a entrada na sala
        sendMsg("Servidor", usrName + " entrou!");
        return;
    }

    public void leaveRoom(String usrName) throws RemoteException{
        //remove o usuario da lista (sala)
    	userList.remove(usrName);
    	//chama o metodo sendMsg para informar sobre a saída
        sendMsg("Servidor", usrName + " saiu!");
        return;
    }

    public void closeRoom() throws RemoteException{
    	//chama o metodo para informar que o servidor fechou a sala
        sendMsg("Servidor", "Sala fechada pelo servidor.");
        //limpa o userList removendo todos os usuários
        userList.clear();
        //armazena a URL da sala
        String roomUrl = "rmi://localhost:2020/" + roomName;
        
        
        try {
        	//desvincula o objeto remoto da sala, do registro RMI
        	//impede que novos clientes acessem o objeto pela URL
            Naming.unbind(roomUrl);
            //desexporta o objeto remoto, libera os recursos associados a ele
            //nao esta mais disponivel p/ acesso remoto, remove o obj do RMI Registry
            UnicastRemoteObject.unexportObject(this, false);
        } catch (MalformedURLException | NotBoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return;
    }

    public String getRoomName(){
        return roomName;
    }
}
