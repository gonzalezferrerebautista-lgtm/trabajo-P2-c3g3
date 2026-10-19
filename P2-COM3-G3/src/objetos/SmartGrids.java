package objetos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SmartGrids implements ISmartGrids {
	
	private HashMap<String, Planta> plantas;
	private HashMap<String, Boolean> actividadPorPlanta;
	
	public SmartGrids() {
		this.plantas = new LinkedHashMap<String, Planta>();
		this.actividadPorPlanta = new LinkedHashMap<String, Boolean>();
	}
	
	@Override
	public void registrarPlantaSolar(String nombre, String direccion, double capacidadMaxima,
			String fechaHoraPuestaEnFuncionamiento, String fuentePrimaria, double eficienciaPromedio,
			double superficieCubierta, int cantidadPaneles, double horasRadiacionDiaria) {
		if (plantas.containsKey(nombre)) {
			throw new RuntimeException("Ya existe una planta con ese nombre");
		}
		if (!esFechaHoraValida(fechaHoraPuestaEnFuncionamiento)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		
		Planta nueva = new Solar(nombre, direccion, capacidadMaxima, true, fechaHoraPuestaEnFuncionamiento,
				eficienciaPromedio, fuentePrimaria, cantidadPaneles, superficieCubierta, horasRadiacionDiaria);
		
		plantas.put(nombre, nueva);
		actividadPorPlanta.put(nombre, true);
		
	}

	@Override
	public void registrarPlantaEolica(String nombre, String direccion, double capacidadMaxima,
			String fechaHoraPuestaEnFuncionamiento, String fuentePrimaria, double eficienciaPromedio,
			int cantidadAerogeneradores, double alturaTorres, double velocidadMinimaViento) {
		if (plantas.containsKey(nombre)) {
			throw new RuntimeException("Ya existe una planta con ese nombre");
		}
		if (!esFechaHoraValida(fechaHoraPuestaEnFuncionamiento)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		
		Planta nueva = new Eolica(nombre, direccion, capacidadMaxima, true, fechaHoraPuestaEnFuncionamiento,
				eficienciaPromedio, fuentePrimaria, cantidadAerogeneradores, alturaTorres, velocidadMinimaViento);
		
		plantas.put(nombre, nueva);
		actividadPorPlanta.put(nombre, true);
	}

	@Override
	public void registrarPlantaHidraulica(String nombre, String direccion, double capacidadMaxima,
			String fechaHoraPuestaEnFuncionamiento, String fuentePrimaria, double eficienciaPromedio,
			int cantidadTurbinas, double capacidadEmbalse) {
		if (plantas.containsKey(nombre)) {
			throw new RuntimeException("Ya existe una planta con ese nombre");
		}
		if (!esFechaHoraValida(fechaHoraPuestaEnFuncionamiento)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		
		Planta nueva = new Hidraulica(nombre, direccion, capacidadMaxima, true, fechaHoraPuestaEnFuncionamiento,
				eficienciaPromedio, fuentePrimaria, cantidadTurbinas, capacidadEmbalse);
		
		plantas.put(nombre, nueva);
		actividadPorPlanta.put(nombre, true);
	}

	@Override
	public void registrarPlantaTermica(String nombre, String direccion, double capacidadMaxima,
			String fechaHoraPuestaEnFuncionamiento, String tipoCombustible, double objetivoEmisionesCo2PorHora,
			double capacidadAlmacenamientoCombustible) {
		if (plantas.containsKey(nombre)) {
			throw new RuntimeException("Ya existe una planta con ese nombre");
		}
		
		if (!esFechaHoraValida(fechaHoraPuestaEnFuncionamiento)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		
		Planta nueva = new Termica(nombre, direccion, capacidadMaxima, true, fechaHoraPuestaEnFuncionamiento,
				tipoCombustible, objetivoEmisionesCo2PorHora, capacidadAlmacenamientoCombustible);
		
		plantas.put(nombre, nueva);
		actividadPorPlanta.put(nombre, true);
	}

	@Override
	public List<String> consultarPlantas() {
		// "{NOMBRE} ({ACTIVA|INACTIVA}): {FECHA_PUESTA_EN_FUNCIONAMIENTO}"
		List<String> resultado = new ArrayList<String>();
		
		for (Planta planta : plantas.values()) {
			StringBuilder sb = new StringBuilder();
			sb.append(planta.getNombre() + " ");
			sb.append(planta.isEstado() ? "(ACTIVA): " : "(INACTIVA): ");
			sb.append(planta.getFechaInicio());
			
			String sp = sb.toString();
			resultado.add(sp);
		}
		
		return resultado;
	}

	@Override
	public double capacidadMinimaCuota(String nombre) {
		if (!plantas.containsKey(nombre)) {
			throw new RuntimeException("No existe una planta con ese nombre");
		}
		Planta planta = plantas.get(nombre);
		double cuotaMax = 1;
		
		if (planta instanceof PlantaRenovable) {
			PlantaRenovable pr = (PlantaRenovable) planta;
			cuotaMax = pr.getCapacidad()*pr.getEficienciaPromedio();
			if (pr instanceof Solar) {
				Solar s = (Solar) pr;
				cuotaMax = cuotaMax * (s.getHorasRadiacion()/24);
			}
		}
		else if (planta instanceof Termica) {
			Termica t = (Termica) planta;
			cuotaMax = t.getCapacidad() / 2;
			
		}
		
		else {
			throw new RuntimeException("Tipo de planta no registrado.");
		}
		
		return cuotaMax;
	}

	@Override
	public void activarPlanta(String nombre, String fechaHora) {
		if (!plantas.containsKey(nombre)) {
			throw new RuntimeException("No existe una planta con ese nombre");
		}
		if (!esFechaHoraValida(fechaHora)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		if (estaActiva(nombre)) {
			throw new RuntimeException("La planta ya esta activa");
		}
		Planta p = plantas.get(nombre);
		p.activar(fechaHora);
		actividadPorPlanta.put(nombre, true);
	}

	@Override
	public void desactivarPlanta(String nombre, String fechaHora) {
		if (!plantas.containsKey(nombre)) {
			throw new RuntimeException("No existe una planta con ese nombre");
		}
		if (!esFechaHoraValida(fechaHora)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		if (!estaActiva(nombre)) {
			throw new RuntimeException("La planta ya esta inactiva");
		}
		Planta p = plantas.get(nombre);
		p.desactivar(fechaHora);
		actividadPorPlanta.put(nombre, false);
	}

	@Override
	public boolean estaActiva(String nombre) {
		if (!plantas.containsKey(nombre)) {
			throw new RuntimeException("No existe una planta con ese nombre");
		}
		return actividadPorPlanta.get(nombre);
	}

	@Override
	public void registrarMonitoreo(String nombre, String fechaHora, double energiaGenerada, int incidencias) {
		if (!plantas.containsKey(nombre)) {
			throw new RuntimeException("No existe una planta con ese nombre");
		}
		if (!esFechaHoraValida(fechaHora)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		if (!estaActiva(nombre)) {
			throw new RuntimeException("La planta esta inactiva");
		}
		if (energiaGenerada < 0 || incidencias < 0) {
			throw new RuntimeException("Los datos no pueden ser negativos.");
		}
		
		Planta planta = this.plantas.get(nombre);
		planta.registrarMonitoreo(fechaHora, energiaGenerada, incidencias);
	}

	@Override
	public void registrarMonitoreo(String nombre, String fechaHora, double energiaGenerada, int incidencias,
			double velocidadViento) {
		if (!plantas.containsKey(nombre)) {
			throw new RuntimeException("No existe una planta con ese nombre");
		}
		if (!esFechaHoraValida(fechaHora)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		if (!estaActiva(nombre)) {
			throw new RuntimeException("La planta esta inactiva");
		}
		if (energiaGenerada < 0 || incidencias < 0 || velocidadViento < 0) {
			throw new RuntimeException("Los datos no pueden ser negativos.");
		}
		if (!(this.plantas.get(nombre) instanceof Eolica)) {
			throw new RuntimeException("No existe una planta eolica con ese nombre");
		}
		
		Eolica planta = (Eolica) this.plantas.get(nombre);
		planta.registrarMonitoreo(fechaHora, energiaGenerada, incidencias, velocidadViento);
	}

	@Override
	public void registrarMonitoreo(String nombre, String fechaHora, double energiaGenerada, int incidencias,
			double co2Emitido, double combustibleUtilizado) {
		if (!plantas.containsKey(nombre)) {
			throw new RuntimeException("No existe una planta con ese nombre");
		}
		if (!esFechaHoraValida(fechaHora)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		if (!estaActiva(nombre)) {
			throw new RuntimeException("La planta esta inactiva");
		}
		
		if (energiaGenerada < 0 || incidencias < 0 || co2Emitido < 0 || combustibleUtilizado < 0) {
			throw new RuntimeException("Los datos no pueden ser negativos.");
		}
		if (!(this.plantas.get(nombre) instanceof Termica)) {
			throw new RuntimeException("No existe una planta con ese nombre");
		}
		
		Termica planta = (Termica) this.plantas.get(nombre);
		planta.registrarMonitoreo(fechaHora, energiaGenerada, incidencias, co2Emitido, combustibleUtilizado);
		
	}

	@Override
	public double totalGenerado(String nombre) {
		if (!plantas.containsKey(nombre)) {
			throw new RuntimeException("No existe una planta con ese nombre");
		}
		Planta planta = plantas.get(nombre);
		return planta.totalGenerado();
	}

	@Override
	public int[] incidenciasPorHora(String nombre) {
		if (!plantas.containsKey(nombre)) {
			throw new RuntimeException("No existe una planta con ese nombre");
		}
		Planta planta = plantas.get(nombre);
		return planta.incidenciasPorHora();
	}

	@Override
	public int[] histogramaCumplimiento(String nombre, String fechaDesde, String fechaHasta) {
		if (!plantas.containsKey(nombre)) {
			throw new RuntimeException("No existe una planta con ese nombre");
		}
		if (!esFechaValida(fechaDesde) || !esFechaValida(fechaHasta)) {
			throw new RuntimeException("La fecha no tiene un formato valido");
		}
		if (LocalDate.parse(fechaDesde).isAfter(LocalDate.parse(fechaHasta))) {
			throw new RuntimeException("fechaDesde es posterior a fechaHasta");
		}
		LocalDate desde = LocalDate.parse(fechaDesde);
		LocalDate hasta = LocalDate.parse(fechaHasta);
		double capacidadMinimaCuota = capacidadMinimaCuota(nombre);
		Planta planta = plantas.get(nombre);
		int[] resultado = new int[24];
		
		for (Map.Entry<LocalDateTime, RegistroMonitoreo> entry : planta.getMonitoreos().entrySet()) {
			LocalDateTime tiempoRegistro = entry.getKey();
			if (!tiempoRegistro.toLocalDate().isBefore(desde) && !tiempoRegistro.toLocalDate().isAfter(hasta)) {
				double generado = entry.getValue().getEnergiaGenerada();
				if (generado >= capacidadMinimaCuota) {
					resultado[tiempoRegistro.getHour()]+=1;
				}
			}
		}
		
		return resultado;
		
	}

	@Override
	public Map<String, Integer> plantasRenovablesPorFuente() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> plantasQueNuncaCumplieronCuota() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String horasSinVelocidadMinimaPorPlantaEolica() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int limpiarMonitoreosSinDatos() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<String> plantasOrdenadasPorEnergiaGenerada() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> plantasInactivasEn(String fechaHora) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static boolean esFechaHoraValida(String texto) {
        try {
            LocalDateTime.parse(texto, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

	public static boolean esFechaValida(String texto) {
        try {
            LocalDate.parse(texto, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
