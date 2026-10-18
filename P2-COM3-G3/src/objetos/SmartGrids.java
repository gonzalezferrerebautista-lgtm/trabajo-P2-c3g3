package objetos;

import java.time.LocalDate;
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registrarMonitoreo(String nombre, String fechaHora, double energiaGenerada, int incidencias,
			double velocidadViento) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registrarMonitoreo(String nombre, String fechaHora, double energiaGenerada, int incidencias,
			double co2Emitido, double combustibleUtilizado) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double totalGenerado(String nombre) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] incidenciasPorHora(String nombre) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] histogramaCumplimiento(String nombre, String fechaDesde, String fechaHasta) {
		// TODO Auto-generated method stub
		return null;
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

}
