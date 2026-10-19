package objetos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RegistroMonitoreoTermico extends RegistroMonitoreo {
	private double cantidadCO2Generado;
	private double cantidadCombustibleConsumido;
	
	public RegistroMonitoreoTermico(String nombrePlanta, LocalDateTime fecha, int incidencias, double energiaGenerada, double cantidadCO2Generado, double cantidadCombustibleConsumido) {
		super(nombrePlanta, fecha, incidencias, energiaGenerada);
		this.cantidadCO2Generado = cantidadCO2Generado;
		this.cantidadCombustibleConsumido = cantidadCombustibleConsumido;
	}

}
