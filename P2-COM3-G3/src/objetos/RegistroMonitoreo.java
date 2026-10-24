package objetos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RegistroMonitoreo {
	private String nombrePlanta;
	private LocalDateTime fecha;
	private int incidencias;
	private double energiaGenerada;

	public RegistroMonitoreo(String nombrePlanta, LocalDateTime fecha, int incidencias, double energiaGenerada) {
		this.nombrePlanta = nombrePlanta;
		this.fecha = fecha;
		this.incidencias = incidencias;
		this.energiaGenerada = energiaGenerada;
	}

	public String getNombrePlanta() {
		return nombrePlanta;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public int getIncidencias() {
		return incidencias;
	}

	public double getEnergiaGenerada() {
		return energiaGenerada;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(fecha + ": ");
		sb.append("\n	- INCIDENCIAS: ").append(incidencias).append(" ");
		sb.append("\n	- ENERGIA GENERADA: ").append(energiaGenerada).append(" ");
		
		return sb.toString();
	}
}
