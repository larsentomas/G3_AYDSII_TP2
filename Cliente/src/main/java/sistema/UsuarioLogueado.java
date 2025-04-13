package sistema;

import java.util.ArrayList;

public class UsuarioLogueado extends Usuario implements Runnable {

    private ArrayList<String> contactos;
    private ArrayList<Conversacion> conversaciones;

    public UsuarioLogueado(String nickname, String ip, int puerto) {
        super(nickname, ip, puerto);
        this.contactos = new ArrayList<>();
        this.conversaciones = new ArrayList<>();
    }



}
