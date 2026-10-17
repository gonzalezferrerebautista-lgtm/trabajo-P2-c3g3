package objetos;

import java.time.LocalDate;

public class RegistroMonitoreoTermico extends RegistroMonitoreo {
	private double cantidadCO2Generado;
	private double cantidadCombustibleConsumido;
	
	public RegistroMonitoreoTermico(String nombrePlanta, LocalDate fecha, int hora, int incidencias, double energiaGenerada, double cantidadCO2Generado, double cantidadCombustibleConsumido) {
		super(nombrePlanta, fecha, hora, incidencias, energiaGenerada);
		this.cantidadCO2Generado = cantidadCO2Generado;
		this.cantidadCombustibleConsumido = cantidadCombustibleConsumido;
	}

}
