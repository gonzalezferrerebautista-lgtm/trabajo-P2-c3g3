package objetos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public abstract class PlantaRenovable extends Planta {
	private double eficienciaPromedio;
	private String fuentePrimaria;

	public PlantaRenovable(String nombre, String direccion, double capacidad, boolean estado, String fechaInicio,
			double eficienciaPromedio, String fuentePrimaria) {
		super(nombre, direccion, capacidad, estado, fechaInicio);
		this.eficienciaPromedio = eficienciaPromedio;
		this.fuentePrimaria = fuentePrimaria;
	}

	public boolean cumpleCuota(String fechaHora) {
		LocalDateTime clave = LocalDateTime.parse(fechaHora);
		RegistroMonitoreo r = obtenerMonitoreo(clave);
		if (r == null) {
			return false;
		}
		return r.getEnergiaGenerada() >= capacidadMinimaCuota();
	}

	public double getEficienciaPromedio() {
		return eficienciaPromedio;
	}

	public String getFuentePrimaria() {
		return fuentePrimaria;
	}

	@Override
	public double capacidadMinimaCuota() {
		return super.getCapacidad() * eficienciaPromedio;
	}
	
	@Override
    public void registrarFuenteSiEsRenovable(Map<String, Integer> fuentePorPlanta) {
        fuentePorPlanta.put(fuentePrimaria, fuentePorPlanta.getOrDefault(fuentePrimaria, 0) + 1);
    }

}
