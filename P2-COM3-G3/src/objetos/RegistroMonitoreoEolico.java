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
	
	public double getVelocidadPromedioViento() {
	   return velocidadPromedioViento;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append("\n	- VELOCIDAD PROMEDIO VIENTO: ").append(velocidadPromedioViento + " ");
		return sb.toString();
	}
}
