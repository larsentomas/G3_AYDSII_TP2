package servidor;

import excepciones.UsuarioExistenteException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ActivoState extends ServidorState {

    public ActivoState(Servidor servidor) {
        super(servidor);
    }

    @Override
    public void start() {
        try {
            servidor.setServerSocket(new ServerSocket(getPuertoPrincipal()));

            // Thread for clients
            new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = getServerSocket().accept();
                        new Thread(new HandlerSolicitudes(clientSocket, this)).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            System.out.println("Falla creando socket para recibir solicitudes de cliente");
        }

        try {
            setServerSocketMonitor(new ServerSocket(getPuertoMonitor()));
            // Thread for monitor
            new Thread(() -> {
                while (true) {
                    try {
                        Socket monitorSocket = getServerSocketMonitor().accept();
                        new Thread(new Monitor(monitorSocket, this)).start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            System.out.println("Falla creando socket para recibir solicitudes de monitor");
        }
    }

    @Override
    public boolean logearCliente(String usuario, String ip, int puerto) throws IOException, UsuarioExistenteException {
        if (validarDireccion(ip, puerto, usuario)) {
            if (!getDirectorio().containsKey(usuario)) { // usuario por primera vez
                UsuarioServidor nuevoUsuario = new UsuarioServidor(usuario, ip, puerto);
                putDirectorio(usuario, nuevoUsuario);
                return true;
            } else {
                UsuarioServidor usuarioExistente = getUsuarioDirectorio(usuario);
                if (!usuarioExistente.isConectado()) {
                    usuarioExistente.setConectado(true);
                    usuarioExistente.setIp(ip);
                    usuarioExistente.setPuerto(puerto);
                    return true;
                }
                return false;
            }
        }
        throw new UsuarioExistenteException(usuario);
    }

    @Override
    public boolean validarDireccion(String ip, int puerto, String name) {
        try {
            String ipServidor = InetAddress.getLocalHost().getHostAddress();
            if (puerto == getPuertoPrincipal() && ip.equalsIgnoreCase(ipServidor)) {
                return false;
            }
        } catch (UnknownHostException e) {
            System.err.println("Error al obtener la dirección IP del servidor: " + e.getMessage());
        }
        for (UsuarioServidor usuario : getDirectorio().values()) {
            if (usuario.getIp().equals(ip) && usuario.getPuerto() == puerto && !usuario.getNombre().equals(name)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ArrayList<String> getDatosDirectorio() {
        return new ArrayList<>(getDirectorio().keySet());
    }

    @Override
    public UsuarioServidor getUsuario(String usuario) {
        return getUsuarioDirectorio(usuario);
    }

    // NO SON DE ESTE

    @Override
    public void resincronizacion() {
        System.out.println("El servidor primario no deberia estar llamando resincronizacion");
    }

    @Override
    public void escuchar(ServerSocket serverSocketEscucha) {
        System.out.println("El servidor primario no deberia estar llamando escuchar");
    }

    @Override
    public void monitorear() {
        System.out.println("El servidor primario no deberia estar llamando monitorear");
    }
}
