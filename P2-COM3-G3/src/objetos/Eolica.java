package objetos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Eolica extends PlantaRenovable {
	private int cantidadAerogeneradores;
	private double alturaTorre;
	private double velocidadMinViento;

	public Eolica(String nombre, String direccion, double capacidad, boolean estado, String fechaInicio,
			double eficienciaPromedio, String fuentePrimaria, int cantidadAerogeneradores, double alturaTorre,
			double velocidadMinViento) {
		super(nombre, direccion, capacidad, estado, fechaInicio, eficienciaPromedio, fuentePrimaria);
		this.cantidadAerogeneradores = cantidadAerogeneradores;
		this.alturaTorre = alturaTorre;
		this.velocidadMinViento = velocidadMinViento;
	}

	public void registrarMonitoreo(String fechaHora, double energiaGenerada, int incidencias, double velocidadViento) {
		if (!SmartGrids.esFechaHoraValida(fechaHora)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		LocalDateTime fechaHoraParseada = LocalDateTime.parse(fechaHora).truncatedTo(ChronoUnit.HOURS);
		if (monitoreos.containsKey(fechaHoraParseada)) {
			throw new RuntimeException("Ya hay un monitoreo para esta fecha + hora.");
		}

		RegistroMonitoreoEolico registro = new RegistroMonitoreoEolico(nombre, fechaHoraParseada, incidencias,
				energiaGenerada, velocidadViento);
		monitoreos.put(fechaHoraParseada, registro);

	}

	@Override
	public boolean cumpleCuota(String fecha, int hora) {
		LocalDateTime clave = LocalDate.parse(fecha).atTime(hora, 0);
		RegistroMonitoreo r = monitoreos.get(clave);
		if (r == null) {
			return false;
		}
		double cuota = getCapacidad() * getEficienciaPromedio();
		return r.getEnergiaGenerada() >= cuota;
	}

}
