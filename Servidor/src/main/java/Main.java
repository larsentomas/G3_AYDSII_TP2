import excepciones.ServidorPrincipalCaidoException;
import servidor.Servidor;
import servidor.ActivoState;
import servidor.PasivoState;

public class Main {

    public static void main(String[] args) {
        int puertoPrincipal = 6000;
        int puertoMonitor = 7000;
        int puertoSecundario= 6001;
        Servidor servidor = new Servidor(puertoPrincipal, puertoMonitor, puertoSecundario);

        if (Servidor.noExisteServidor(puertoPrincipal)) {
            System.out.println("SERVIDOR PRIMARIO");
            servidor.setState(new ActivoState(servidor));
        } else if (Servidor.noExisteServidor(puertoSecundario)) {
            System.out.println("SERVIDOR SECUNDARIO");
            servidor.setState(new PasivoState(servidor));
        } else {
            System.out.println("No se puede iniciar el servidor. Ya existe un servidor en los puertos especificados.");
        }

        try {
            servidor.start();
        } catch (ServidorPrincipalCaidoException e) {
            System.out.println("El servidor principal, se cayo. El secundario comienza a actuar como primario.");
            try {
                servidor.start();
            } catch (ServidorPrincipalCaidoException ex) {
                System.out.println("El servidor primario no deberia lanzar ServidorPrincipalCaidoException, ya que es el primario.");
            }
        }

    }

}


