package objetos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Termica extends Planta {
	private String tipoCombustible;
	private double objetivoEmision;
	private double capacidadAlmacenamientoCombustible;
	public Termica(String nombre, String direccion, double capacidad, boolean estado, String fechaInicio,
			String tipoCombustible, double objetivoEmision, double capacidadAlmacenamientoCombustible) {
		super(nombre, direccion, capacidad, estado, fechaInicio);
		this.tipoCombustible = tipoCombustible;
		this.objetivoEmision = objetivoEmision;
		this.capacidadAlmacenamientoCombustible = capacidadAlmacenamientoCombustible;
	}
	
	public void registrarMonitoreo(String fechaHora, double energiaGenerada, int incidencias, double co2Emitido, double combustibleUtilizado) {
		if (!SmartGrids.esFechaHoraValida(fechaHora)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		LocalDateTime fechaHoraParseada = LocalDateTime.parse(fechaHora).truncatedTo(ChronoUnit.HOURS);
		if (monitoreos.containsKey(fechaHoraParseada)) {
			throw new RuntimeException("Ya hay un monitoreo para esta fecha + hora.");
		}
		
		RegistroMonitoreoTermico registro = new RegistroMonitoreoTermico(nombre, fechaHoraParseada, incidencias, energiaGenerada, co2Emitido, combustibleUtilizado);
		monitoreos.put(fechaHoraParseada, registro);
		
	}
	
	public String getTipoCombustible() {
		return tipoCombustible;
	}
	public double getObjetivoEmision() {
		return objetivoEmision;
	}
	public double getCapacidadAlmacenamiento() {
		return capacidadAlmacenamientoCombustible;
	}

	@Override
	public double capacidadMinimaCuota() {
		return super.getCapacidad()/2;
	}
	
}
