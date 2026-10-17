package objetos;

import java.time.LocalDate;

public class RegistroMonitoreoEolico extends RegistroMonitoreo {
	private double velocidadPromedioViento;
	
	public RegistroMonitoreoEolico(String nombrePlanta, LocalDate fecha, int hora, int incidencias, double energiaGenerada, double velocidadPromedioViento) {
		super(nombrePlanta, fecha, hora, incidencias, energiaGenerada);
		this.velocidadPromedioViento = velocidadPromedioViento;
	}

}
