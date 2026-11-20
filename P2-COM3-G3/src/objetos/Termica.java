package objetos;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Termica extends Planta {
	private String tipoCombustible;
	private double objetivoEmision;
	private double capacidadAlmacenamientoCombustible;

	@Override
	public void registrarMonitoreo(String fechaHora, double energiaGenerada, int incidencias) {
	    throw new RuntimeException("Las plantas térmicas requieren datos adicionales.");
	}
	
	public Termica(String nombre, String direccion, double capacidad, boolean estado, String fechaInicio,
			String tipoCombustible, double objetivoEmision, double capacidadAlmacenamientoCombustible) {
		super(nombre, direccion, capacidad, estado, fechaInicio);
		this.tipoCombustible = tipoCombustible;
		this.objetivoEmision = objetivoEmision;
		this.capacidadAlmacenamientoCombustible = capacidadAlmacenamientoCombustible;
	}

	@Override
	public boolean cumpleCuota(String fechaHora) {
		
		LocalDateTime clave = LocalDateTime.parse(fechaHora).truncatedTo(ChronoUnit.HOURS);
		
		RegistroMonitoreo r = obtenerMonitoreo(clave);
		if (r == null) {
			return false;
		}
		RegistroMonitoreoTermico rt = (RegistroMonitoreoTermico) r;
		return rt.getEnergiaGenerada() >= capacidadMinimaCuota() && rt.getCantidadCO2Generado() < objetivoEmision;

	}

	@Override
	public void registrarMonitoreo(String fechaHora, double energiaGenerada, int incidencias, double co2Emitido,
			double combustibleUtilizado) {
		if (!SmartGrids.esFechaHoraValida(fechaHora)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		LocalDateTime fechaHoraOriginal = LocalDateTime.parse(fechaHora);
		LocalDateTime fechaHoraTruncada = fechaHoraOriginal.truncatedTo(ChronoUnit.HOURS);
		super.validarRegistroMonitoreo(fechaHoraOriginal, fechaHoraOriginal);

		RegistroMonitoreoTermico registro = new RegistroMonitoreoTermico(nombre, fechaHoraTruncada, incidencias,
				energiaGenerada, co2Emitido, combustibleUtilizado);
		guardarMonitoreo(fechaHoraOriginal, fechaHoraTruncada, registro);
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
		return super.getCapacidad() / 2;
	}

}
