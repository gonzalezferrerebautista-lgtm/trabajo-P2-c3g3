package objetos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RegistroMonitoreoEolico extends RegistroMonitoreo {
	private double velocidadPromedioViento;

	public RegistroMonitoreoEolico(String nombrePlanta, LocalDateTime fecha, int incidencias, double energiaGenerada,
			double velocidadPromedioViento) {
		super(nombrePlanta, fecha, incidencias, energiaGenerada);
		this.velocidadPromedioViento = velocidadPromedioViento;
	}

}
