package servidor;

import common.Mensaje;
import excepciones.ServidorPrincipalCaidoException;
import excepciones.UsuarioExistenteException;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

public abstract class ServidorState {
    protected Servidor servidor;

    public ServidorState(Servidor servidor) {
        this.servidor = servidor;
    }

    // Metodos a implementar
    public abstract void start() throws ServidorPrincipalCaidoException;

    public abstract boolean logearCliente(String usuario, String ip, int puerto) throws IOException, UsuarioExistenteException;
    public abstract boolean validarDireccion(String ip, int puerto, String name);
    public abstract ArrayList<String> getDatosDirectorio();
    public abstract UsuarioServidor getUsuario(String usuario);

    public abstract void resincronizacion();
    public abstract void escuchar(ServerSocket serverSocketEscucha);
    public abstract void monitorear();

    // Metodos compartidos
    public static boolean noExisteServidor(int puerto) {
        return Servidor.noExisteServidor(puerto);
    }
    public void agregarDirectorio(String usuario, UsuarioServidor usuarioServidor) {
        servidor.putDirectorio(usuario, usuarioServidor);
    }
    public void agregarMensajeACola(String usuario, Mensaje mensaje) {
        if (!getColaMensajes().containsKey(usuario)) {
            getColaMensajes().put(usuario, new LinkedList<>());
        }
        Queue<Mensaje> mensajes = servidor.getColaMensajes().get(usuario);
        mensajes.add(mensaje);
    }

    // Getters y setters

    public ServerSocket getServerSocket() {
        return servidor.getServerSocket();
    }

    public void setServerSocket(ServerSocket serverSocket) {
        servidor.setServerSocket(serverSocket);
    }

    public ServerSocket getServerSocketMonitor() {
        return servidor.serverSocketMonitor;
    }

    public void setServerSocketMonitor(ServerSocket serverSocketMonitor) {
        servidor.setServerSocketMonitor(serverSocketMonitor);
    }

    public HashMap<String, UsuarioServidor> getDirectorio() {
        return servidor.getDirectorio();
    }

    public void setDirectorio(HashMap<String, UsuarioServidor> directorio) {
        servidor.setDirectorio(directorio);
    }

    public Map<String, Queue<Mensaje>> getColaMensajes() {
        return servidor.getColaMensajes();
    }

    public boolean isRecibioEcho() {
        return servidor.isRecibioEcho();
    }

    public void setRecibioEcho(boolean recibioEcho) {
        servidor.setRecibioEcho(recibioEcho);
    }

    public int getPuertoPrincipal() {
        return servidor.getPuertoPrincipal();
    }

    public int getPuertoMonitor() {
        return servidor.puertoMonitor;
    }

    public int getPuertoSecundario() {
        return servidor.puertoSecundario;
    }

    public void setColaMensajes(Map<String, Queue<Mensaje>> colaMensajes) {
        servidor.setColaMensajes(colaMensajes);
    }

    public Queue<Mensaje> getColaMensajesUsuario(String u) {
        return servidor.getColaMensajesUsuario(u);
    }

    public void putDirectorio(String s, UsuarioServidor usuarioServidor) {
        servidor.putDirectorio(s, usuarioServidor);
    }

    public UsuarioServidor getUsuarioDirectorio(String s) {
        return servidor.getUsuarioDirectorio(s);
    }
}
