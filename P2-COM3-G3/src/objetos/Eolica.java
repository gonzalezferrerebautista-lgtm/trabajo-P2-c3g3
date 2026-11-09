package objetos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.TreeMap;

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

	@Override
	public void registrarMonitoreo(String fechaHora, double energiaGenerada, int incidencias) {
	    throw new RuntimeException("Las plantas eólicas/térmicas requieren datos adicionales.");
	}
	
	public void registrarMonitoreo(String fechaHora, double energiaGenerada, int incidencias, double velocidadViento) {
		if (!SmartGrids.esFechaHoraValida(fechaHora)) {
			throw new RuntimeException("La fecha y hora no tienen un formato valido");
		}
		LocalDateTime fechaHoraOriginal = LocalDateTime.parse(fechaHora);
		LocalDateTime fechaHoraTruncada = fechaHoraOriginal.truncatedTo(ChronoUnit.HOURS);
		super.validarRegistroMonitoreo(fechaHoraOriginal, fechaHoraTruncada);

		RegistroMonitoreoEolico registro = new RegistroMonitoreoEolico(nombre, fechaHoraTruncada, incidencias,
				energiaGenerada, velocidadViento);
		guardarMonitoreo(fechaHoraOriginal, fechaHoraTruncada, registro);
	}
	
	@Override
	public String reportePocoViento() {
		return horasConPocoViento() + "\n";
	}
	
	
	public String horasConPocoViento() {
	    StringBuilder texto = new StringBuilder();
	    texto.append("- ").append(nombre).append(": ").append(velocidadMinViento).append(" m/s \n");
	    TreeMap<Integer, Double> maximoEnHoras = new TreeMap<Integer, Double>();
	    for(RegistroMonitoreo monitoreo: obtenerMonitoreos()) {
	    		//Cargo los mejores tiempos por hora.
	    	    RegistroMonitoreoEolico registro = (RegistroMonitoreoEolico) monitoreo;
	    		if(maximoEnHoras.containsKey(registro.getFecha().getHour())) {
	    		     double velocidadMaxima = Math.max(registro.getVelocidadPromedioViento(), maximoEnHoras.get(registro.getFecha().getHour()));
	    		     maximoEnHoras.put(registro.getFecha().getHour() , velocidadMaxima);
	    		}
	    		else {
	    			 maximoEnHoras.put(registro.getFecha().getHour() , registro.getVelocidadPromedioViento() );
	    		}
	    }
        //Analizo si los mejores tiempos de cada hora cumplen con la velocidad, en caso de que no cumplan se mandan con el mejor resultado.
	    for(int hora : maximoEnHoras.keySet()) {
	    	
	    	
	    	if(maximoEnHoras.get(hora) < velocidadMinViento) {
	    		String horaString = Integer.toString(hora);
		    	if (horaString.length() == 1) {
		    		horaString = "0"+horaString;
		    	}
	    		texto.append("     "+ horaString + ": " + maximoEnHoras.get(hora) + " m/s \n");
	    	}
	    }
	    return texto.toString();
	}

}
