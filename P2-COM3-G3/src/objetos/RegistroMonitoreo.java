package objetos;

import java.time.LocalDate;

public class RegistroMonitoreo {
	private String nombrePlanta;
	private LocalDate fecha;
	private int hora;
	private int incidencias;
	protected double energiaGenerada;
	
	public RegistroMonitoreo(String nombrePlanta, LocalDate fecha, int hora, int incidencias, double energiaGenerada) {
		this.nombrePlanta = nombrePlanta;
		this.fecha = fecha;
		this.hora = hora;
		this.incidencias = incidencias;
		this.energiaGenerada = energiaGenerada;
	}
}
