package servidor;

import common.Mensaje;
import excepciones.ServidorPrincipalCaidoException;
import excepciones.UsuarioExistenteException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Servidor {
    public ServidorBase servidor;

    protected ServerSocket serverSocket;
    protected ServerSocket serverSocketMonitor;
    protected HashMap<String, UsuarioServidor> directorio = new HashMap<>();
    protected final Map<String, Queue<Mensaje>> colaMensajes = new ConcurrentHashMap<>();
    protected boolean recibioEcho;
    protected final int puertoPrincipal;
    protected final int puertoMonitor;
    protected final int puertoSecundario;

    public Servidor(int puertoPrincipal, int puertoMonitor, int puertoSecundario) {
        this.puertoPrincipal = puertoPrincipal;
        this.puertoMonitor = puertoMonitor;
        this.puertoSecundario = puertoSecundario;
    }

    public void start() throws ServidorPrincipalCaidoException {
        servidor.start();
    };

    public boolean logearCliente(String usuario, String ip, int puerto) throws IOException, UsuarioExistenteException {return servidor.logearCliente(usuario, ip, puerto);}
    public boolean validarDireccion(String ip, int puerto, String name) {return servidor.validarDireccion(ip, puerto, name);}
    public ArrayList<String> getDatosDirectorio() {return servidor.getDatosDirectorio();}
    public UsuarioServidor getUsuario(String usuario) {return servidor.getUsuario(usuario);}
    public void resincronizacion() {servidor.resincronizacion();};
    public void escuchar(ServerSocket serverSocketEscucha) {servidor.escuchar(serverSocketEscucha);}
    public void monitorear(ServerSocket serverSocketMonitor) throws ServidorPrincipalCaidoException {servidor.monitorear();}
    public void agregarDirectorio(String usuario, UsuarioServidor usuarioServidor) {servidor.agregarDirectorio(usuario, usuarioServidor);}
    public void agregarMensajeACola(String usuario, Mensaje mensaje) {servidor.agregarMensajeACola(usuario, mensaje);}

    public static boolean noExisteServidor(int puerto) {
        try (ServerSocket serverSocket = new ServerSocket()) {
        InetSocketAddress direccion = new InetSocketAddress(InetAddress.getLocalHost().getHostName(), puerto);
        serverSocket.setReuseAddress(true);
        serverSocket.bind(direccion);
        return true;
    } catch (IOException e) {
        return false;
    }}

    // Getters y setters

    public ServidorBase getServidor() {
        return servidor;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public ServerSocket getServerSocketMonitor() {
        return serverSocketMonitor;
    }

    public void setServerSocketMonitor(ServerSocket serverSocketMonitor) {
        this.serverSocketMonitor = serverSocketMonitor;
    }

    public HashMap<String, UsuarioServidor> getDirectorio() {
        return directorio;
    }

    public void setDirectorio(HashMap<String, UsuarioServidor> directorio) {
        this.directorio = directorio;
    }

    public Map<String, Queue<Mensaje>> getColaMensajes() {
        return colaMensajes;
    }

    public Queue<Mensaje> getColaMensajesUsuario(String s) {
        return colaMensajes.get(s);
    }

    public boolean isRecibioEcho() {
        return recibioEcho;
    }

    public void setRecibioEcho(boolean recibioEcho) {
        this.recibioEcho = recibioEcho;
    }

    public int getPuertoPrincipal() {
        return puertoPrincipal;
    }

    public int getPuertoMonitor() {
        return puertoMonitor;
    }

    public int getPuertoSecundario() {
        return puertoSecundario;
    }

    public void putDirectorio(String s, UsuarioServidor usuarioServidor) {
        this.directorio.put(s, usuarioServidor);
    }

    public UsuarioServidor getUsuarioDirectorio(String s) {
        return this.directorio.get(s);
    }

    public void sacarDeCola(String s) {
        colaMensajes.remove(s);
    }

    public void setColaMensajes(Map<String, Queue<Mensaje>> colaMensajes) {
        this.colaMensajes.clear();
        this.colaMensajes.putAll(colaMensajes);
    }

    // PATRON STATE

    public void cambiarServidor(ServidorBase servidor) {
        this.servidor = servidor;
    }

    public void setServidor(ServidorBase servidor) {
        this.servidor = servidor;
    }
}
