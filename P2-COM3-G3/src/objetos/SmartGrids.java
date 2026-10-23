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
import java.util.Iterator;

public class SmartGrids implements ISmartGrids {

	private HashMap<String, Planta> plantas;

	public SmartGrids() {
		this.plantas = new LinkedHashMap<String, Planta>();
	}

	@Override
	public void registrarPlantaSolar(String nombre, String direccion, double capacidadMaxima,
			String fechaHoraPuestaEnFuncionamiento, String fuentePrimaria, double eficienciaPromedio,
			double superficieCubierta, int cantidadPaneles, double horasRadiacionDiaria) {

		validacionesRegistroPlanta(nombre, fechaHoraPuestaEnFuncionamiento);

		Planta nueva = new Solar(nombre, direccion, capacidadMaxima, true, fechaHoraPuestaEnFuncionamiento,
				eficienciaPromedio, fuentePrimaria, cantidadPaneles, superficieCubierta, horasRadiacionDiaria);

		plantas.put(nombre, nueva);

	}

	@Override
	public void registrarPlantaEolica(String nombre, String direccion, double capacidadMaxima,
			String fechaHoraPuestaEnFuncionamiento, String fuentePrimaria, double eficienciaPromedio,
			int cantidadAerogeneradores, double alturaTorres, double velocidadMinimaViento) {

		validacionesRegistroPlanta(nombre, fechaHoraPuestaEnFuncionamiento);

		Planta nueva = new Eolica(nombre, direccion, capacidadMaxima, true, fechaHoraPuestaEnFuncionamiento,
				eficienciaPromedio, fuentePrimaria, cantidadAerogeneradores, alturaTorres, velocidadMinimaViento);

		plantas.put(nombre, nueva);
	}

	@Override
	public void registrarPlantaHidraulica(String nombre, String direccion, double capacidadMaxima,
			String fechaHoraPuestaEnFuncionamiento, String fuentePrimaria, double eficienciaPromedio,
			int cantidadTurbinas, double capacidadEmbalse) {

		validacionesRegistroPlanta(nombre, fechaHoraPuestaEnFuncionamiento);

		Planta nueva = new Hidraulica(nombre, direccion, capacidadMaxima, true, fechaHoraPuestaEnFuncionamiento,
				eficienciaPromedio, fuentePrimaria, cantidadTurbinas, capacidadEmbalse);

		plantas.put(nombre, nueva);
	}

	@Override
	public void registrarPlantaTermica(String nombre, String direccion, double capacidadMaxima,
			String fechaHoraPuestaEnFuncionamiento, String tipoCombustible, double objetivoEmisionesCo2PorHora,
			double capacidadAlmacenamientoCombustible) {

		validacionesRegistroPlanta(nombre, fechaHoraPuestaEnFuncionamiento);

		Planta nueva = new Termica(nombre, direccion, capacidadMaxima, true, fechaHoraPuestaEnFuncionamiento,
				tipoCombustible, objetivoEmisionesCo2PorHora, capacidadAlmacenamientoCombustible);

		plantas.put(nombre, nueva);
	}

	@Override
	public List<String> consultarPlantas() {
		// "{NOMBRE} ({ACTIVA|INACTIVA}): {FECHA_PUESTA_EN_FUNCIONAMIENTO}"
		List<String> resultado = new ArrayList<String>();

		for (Planta planta : plantas.values()) {
			resultado.add(planta.getDetalle());
		}

		return resultado;
	}

	@Override
	public double capacidadMinimaCuota(String nombre) {
		if (!existePlanta(nombre)) {
			throw new RuntimeException("No existe una planta con ese nombre");
		}
		Planta planta = plantas.get(nombre);
		return planta.capacidadMinimaCuota();
	}

	@Override
	public void activarPlanta(String nombre, String fechaHora) {
		validacionesEstadoPlanta(nombre, fechaHora);
		if (estaActiva(nombre)) {
			throw new RuntimeException("La planta ya esta activa");
		}
		Planta p = plantas.get(nombre);
		p.activar(fechaHora);
	}

	@Override
	public void desactivarPlanta(String nombre, String fechaHora) {
		validacionesEstadoPlanta(nombre, fechaHora);
		if (!estaActiva(nombre)) {
			throw new RuntimeException("La planta ya esta inactiva");
		}
		Planta p = plantas.get(nombre);
		p.desactivar(fechaHora);
	}

	@Override
	public boolean estaActiva(String nombre) {
		if (!existePlanta(nombre)) {
			throw new RuntimeException("No existe una planta con ese nombre");
		}
		return plantas.get(nombre).isEstado();
	}

	@Override
	public void registrarMonitoreo(String nombre, String fechaHora, double energiaGenerada, int incidencias) {
		validacionesRegistrarMonitoreo(nombre, fechaHora, energiaGenerada, incidencias);

		Planta planta = this.plantas.get(nombre);
		planta.registrarMonitoreo(fechaHora, energiaGenerada, incidencias);
	}

	@Override
	public void registrarMonitoreo(String nombre, String fechaHora, double energiaGenerada, int incidencias,
			double velocidadViento) {

		validacionesRegistrarMonitoreo(nombre, fechaHora, energiaGenerada, incidencias);

		if (velocidadViento < 0) {
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

		validacionesRegistrarMonitoreo(nombre, fechaHora, energiaGenerada, incidencias);

		if (co2Emitido < 0 || combustibleUtilizado < 0) {
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

		return plantas.get(nombre).histogramaCumplimiento(nombre, fechaDesde, fechaHasta);

	}

	@Override
	public Map<String, Integer> plantasRenovablesPorFuente() {
		Map<String, Integer> cantPlantasRenovablesPorFuente;
		cantPlantasRenovablesPorFuente = new HashMap<>();
		for (Planta p : plantas.values()) {
			if (p instanceof PlantaRenovable) {
				PlantaRenovable pr = (PlantaRenovable) p;
				String fuente = pr.getFuentePrimaria();
				if (cantPlantasRenovablesPorFuente.containsKey(fuente)) {
					int cantActual = cantPlantasRenovablesPorFuente.get(fuente);
					cantPlantasRenovablesPorFuente.put(fuente, cantActual + 1);
				} else {
					cantPlantasRenovablesPorFuente.put(fuente, 1);
				}

			}
		}
		return cantPlantasRenovablesPorFuente;

	}

	@Override
	public List<String> plantasQueNuncaCumplieronCuota() {
		List<String> plantasQueNuncaCumplieronCuota = new ArrayList<>();
		for (Planta p : plantas.values()) {
			int cumplio = 0;
			for (LocalDateTime fechaHora : p.getMonitoreos().keySet()) {
				String fecha = fechaHora.toString();
				
				if (p.cumpleCuota(fecha)) {
					cumplio++;
				}
			}
			if (cumplio == 0) {
				plantasQueNuncaCumplieronCuota.add(p.getNombre());
			}
		}
		return plantasQueNuncaCumplieronCuota;

	}

	@Override //A charlar por el Junit. Se toma el mayor pero en el Junit se pide que halla dos (no entiendo)
	public String horasSinVelocidadMinimaPorPlantaEolica() {
		StringBuilder texto = new StringBuilder();
	    texto.append("Plantas Eolicas: Horas con poco viento \n");
	    for(Planta planta: plantas.values()) {
	    	if(planta instanceof Eolica) {
	    		Eolica plantaEolica = (Eolica) planta;
	    		texto.append(plantaEolica.horasConPocoViento() + "\n");
	    	}
	    }
	    System.out.println(texto.toString());
		return texto.toString();
	}

	@Override
	public int limpiarMonitoreosSinDatos() {
		int cantBorrados = 0;
		for (Planta p : plantas.values()) {
			Iterator<RegistroMonitoreo> iterator = p.getMonitoreos().values().iterator();
			while (iterator.hasNext()) {
				RegistroMonitoreo r = iterator.next();
				if (r.getEnergiaGenerada() == 0 && r.getIncidencias() == 0) {
					iterator.remove();
					cantBorrados++;
				}
			}

		}
		return cantBorrados;
	}

	@Override
	public List<String> plantasOrdenadasPorEnergiaGenerada() {
		List<String> lista = new ArrayList<String>();
		double[] arrayDeEnergia = new double[plantas.size()];
		String[] arrayDeNombreDePlanta = new String[plantas.size()];
		int indice = 0;
		//Cargo los array, para luego unicamente comparar el array de plantas.
		for(Planta planta : plantas.values()) {
			arrayDeEnergia[indice] = planta.totalGenerado();
			arrayDeNombreDePlanta[indice] = planta.getNombre();
			indice += 1;
		}
		//Utilizo metodo bubleShort hasta que no se realize modificacion implementando boolean.
		indice = 0; //vamos a ir restando uno, pues por cada pasada uno ira a la ultima ubicacion
		boolean seguir = true;
		while(seguir) {
			seguir = false;
			for(int i = 1; i < arrayDeEnergia.length - indice ; i++) {
				if(arrayDeEnergia[i-1] > arrayDeEnergia[i]) {
					seguir = true;
					double aux1 = arrayDeEnergia[i-1];
					arrayDeEnergia[i-1] = arrayDeEnergia[i];
					arrayDeEnergia[i] = aux1;
					String aux2 = arrayDeNombreDePlanta[i-1];
					arrayDeNombreDePlanta[i-1] = arrayDeNombreDePlanta[i];
					arrayDeNombreDePlanta[i] = aux2;
				}
			}
			lista.add(arrayDeNombreDePlanta[arrayDeEnergia.length - indice - 1]+": "+arrayDeEnergia[arrayDeEnergia.length - indice - 1]);
			indice += 1;
		}
		while(indice < arrayDeEnergia.length) {
			lista.add(arrayDeNombreDePlanta[arrayDeEnergia.length - indice - 1]+ ": " +arrayDeEnergia[arrayDeEnergia.length - indice - 1]);
			indice += 1;
		}
		return lista;
	}

	@Override 
	public List<String> plantasInactivasEn(String fechaHora) {
		if(!esFechaHoraValida(fechaHora)) {
		   throw new RuntimeException("La fecha no tiene un formato valido");
		}
		List<String> plantasInactivas = new ArrayList<>();
		for (Planta planta : plantas.values()) {
			if (!planta.estaActivaEn(fechaHora)) {
				plantasInactivas.add(planta.getNombre());
			}

		}
		return plantasInactivas;
	}

	protected static boolean esFechaHoraValida(String texto) {
		try {
			LocalDateTime.parse(texto, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	protected static boolean esFechaValida(String texto) {
		try {
			LocalDate.parse(texto, DateTimeFormatter.ISO_LOCAL_DATE);
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	private void validacionesRegistroPlanta(String nombre, String fechaHoraPuestaEnFuncionamiento) {
		if (existePlanta(nombre)) {
			throw new RuntimeException("Ya existe una planta con ese nombre");
		}
		if (!esFechaHoraValida(fechaHoraPuestaEnFuncionamiento)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
	}

	private void validacionesEstadoPlanta(String nombre, String fechaHora) {
		if (!existePlanta(nombre)) {
			throw new RuntimeException("No existe una planta con ese nombre");
		}
		if (!esFechaHoraValida(fechaHora)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		
	}

	private void validacionesRegistrarMonitoreo(String nombre, String fechaHora, double energiaGenerada,
			int incidencias) {
		if (!existePlanta(nombre)) {
			throw new RuntimeException("No existe una planta con ese nombre");
		}
		if (!esFechaHoraValida(fechaHora)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		Planta planta = plantas.get(nombre);
		if (!planta.estaActivaEn(fechaHora)) {
			throw new RuntimeException("La planta esta inactiva");
		}
		if (energiaGenerada < 0 || incidencias < 0) {
			throw new RuntimeException("Los datos no pueden ser negativos.");
		}
	}

	private boolean existePlanta(String nombre) {
		return plantas.containsKey(nombre);
	}
}
