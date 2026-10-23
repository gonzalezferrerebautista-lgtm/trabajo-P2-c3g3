package objetos;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Test ampliado para SmartGrids. Complementa (no reemplaza) a SmartGridsTest.
 *
 * Los tests se apoyan en lo que dice la interfaz y los enunciados. Los que
 * dependen de una interpretacion estan marcados con "CRITERIO" en el comentario.
 * Si alguno de esos no coincide con lo que decidio tu grupo, revisa el enunciado
 * o consulta con tu docente antes de borrarlo.
 */
public class SuperMegaTest {

    private static final String PUESTA = "2020-01-01T00:00:00";
    private static final String DIA1 = "2026-03-01";
    private static final String DIA2 = "2026-03-02";
    private static final String DIA3 = "2026-03-03";

    private ISmartGrids sistema;

    @Before
    public void setUp() {
        sistema = new SmartGrids();
        // cuota = 1000 * 0.5 * 12 / 24 = 250
        sistema.registrarPlantaSolar("Solar1", "Ruta 1", 1000, PUESTA, "Sol", 0.5, 5000, 2000, 12);
        // cuota = 400 * 0.5 = 200  (velocidad minima de viento = 5)
        sistema.registrarPlantaEolica("Eolica1", "Ruta 2", 400, PUESTA, "Viento", 0.5, 10, 80, 5);
        // cuota = 800 * 0.25 = 200
        sistema.registrarPlantaHidraulica("Hidro1", "Ruta 3", 800, PUESTA, "Agua", 0.25, 4, 1000);
        // cuota = 600 / 2 = 300 y CO2 < 50
        sistema.registrarPlantaTermica("Termica1", "Ruta 4", 600, PUESTA, "Gas", 50, 900);
    }

    /** Arma "yyyy-MM-dd'T'HH:00:00" para un dia y una hora. */
    private static String h(String dia, int hora) {
        return String.format("%sT%02d:00:00", dia, hora);
    }

    /** Devuelve las horas listadas bajo una planta en el reporte de la consigna 10. */
    private static List<Integer> horasListadas(String reporte, String planta) {
        List<Integer> horas = new ArrayList<>();
        boolean dentro = false;
        for (String linea : reporte.split("\\R")) {
            String l = linea.trim();
            if (l.startsWith("-")) {
                dentro = l.substring(1).trim().startsWith(planta + ":");
            } else if (dentro && l.matches("\\d{2}:.*")) {
                horas.add(Integer.parseInt(l.substring(0, 2)));
            }
        }
        return horas;
    }

    // ------------------------------------------------------------------
    // 1. Registro y consulta de plantas
    // ------------------------------------------------------------------

    @Test
    public void sistemaNuevoNoTienePlantasNiResultados() {
        ISmartGrids vacio = new SmartGrids();
        assertTrue(vacio.consultarPlantas().isEmpty());
        assertTrue(vacio.plantasRenovablesPorFuente().isEmpty());
        assertTrue(vacio.plantasQueNuncaCumplieronCuota().isEmpty());
        assertTrue(vacio.plantasOrdenadasPorEnergiaGenerada().isEmpty());
        assertTrue(vacio.plantasInactivasEn(h(DIA1, 10)).isEmpty());
        assertEquals(0, vacio.limpiarMonitoreosSinDatos());
        String reporte = vacio.horasSinVelocidadMinimaPorPlantaEolica();
        assertNotNull(reporte);
        assertTrue(reporte.trim().startsWith("Plantas Eolicas: Horas con poco viento"));
    }

    @Test
    public void nombreDuplicadoSeRechazaEntreTodosLosTipos() {
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar("Eolica1", "x", 10, PUESTA, "Sol", 0.5, 1, 1, 1));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaEolica("Hidro1", "x", 10, PUESTA, "Viento", 0.5, 1, 1, 1));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaHidraulica("Termica1", "x", 10, PUESTA, "Agua", 0.5, 1, 1));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaTermica("Solar1", "x", 10, PUESTA, "Gas", 1, 1));
        assertEquals(4, sistema.consultarPlantas().size());
    }

    @Test
    public void datosFaltantesOInvalidosSeRechazan() {
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar(null, "x", 10, PUESTA, "Sol", 0.5, 1, 1, 1));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar("", "x", 10, PUESTA, "Sol", 0.5, 1, 1, 1));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar("N1", null, 10, PUESTA, "Sol", 0.5, 1, 1, 1));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar("N2", "x", 10, null, "Sol", 0.5, 1, 1, 1));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar("N3", "x", 10, "ayer", "Sol", 0.5, 1, 1, 1));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar("N4", "x", 10, "2026-13-45T00:00:00", "Sol", 0.5, 1, 1, 1));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar("N5", "x", 10, "2020-01-01", "Sol", 0.5, 1, 1, 1));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar("N6", "x", -10, PUESTA, "Sol", 0.5, 1, 1, 1));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar("N7", "x", 10, PUESTA, null, 0.5, 1, 1, 1));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaTermica("N8", "x", 10, PUESTA, null, 1, 1));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaEolica("N9", "x", 10, "ayer", "Viento", 0.5, 1, 1, 1));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaHidraulica("N10", "x", 10, "ayer", "Agua", 0.5, 1, 1));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaTermica("N11", "x", 10, "ayer", "Gas", 1, 1));
        assertEquals(4, sistema.consultarPlantas().size());
    }

    @Test
    public void unRegistroFallidoNoDejaLaPlantaMedioCargada() {
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar("Fallida", "x", 10, "fecha-mala", "Sol", 0.5, 1, 1, 1));
        assertEquals(4, sistema.consultarPlantas().size());
        // el nombre debe seguir disponible
        sistema.registrarPlantaSolar("Fallida", "x", 10, PUESTA, "Sol", 0.5, 1, 1, 1);
        assertEquals(5, sistema.consultarPlantas().size());
    }

    @Test
    public void consultarPlantasMantieneOrdenDeRegistroYFormato() {
        sistema.registrarPlantaHidraulica("Atucha II", "x", 100, "2014-06-27T09:00:00", "Agua", 0.5, 1, 1);
        List<String> plantas = sistema.consultarPlantas();
        assertEquals(5, plantas.size());
        assertEquals("Atucha II (ACTIVA): 2014-06-27T09:00:00", plantas.get(4));
        assertEquals("Solar1 (ACTIVA): " + PUESTA, plantas.get(0));
    }

    @Test
    public void consultarPlantasReflejaElEstado() {
        sistema.desactivarPlanta("Eolica1", h(DIA1, 9));
        List<String> plantas = sistema.consultarPlantas();
        assertEquals("Eolica1 (INACTIVA): " + PUESTA, plantas.get(1));
        assertEquals("Solar1 (ACTIVA): " + PUESTA, plantas.get(0));

        sistema.activarPlanta("Eolica1", h(DIA1, 12));
        assertEquals("Eolica1 (ACTIVA): " + PUESTA, sistema.consultarPlantas().get(1));
    }

    @Test
    public void modificarLaListaDevueltaNoAfectaAlSistema() {
        List<String> plantas = sistema.consultarPlantas();
        try {
            plantas.clear();
        } catch (UnsupportedOperationException e) {
            // tambien es valido devolver una lista inmodificable
        }
        assertEquals(4, sistema.consultarPlantas().size());
    }

    // ------------------------------------------------------------------
    // 2. Cuota minima
    // ------------------------------------------------------------------

    @Test
    public void capacidadMinimaConOtrosValores() {
        sistema.registrarPlantaSolar("Solar2", "x", 2400, PUESTA, "Sol", 0.5, 1, 1, 6);
        sistema.registrarPlantaEolica("Eolica2", "x", 1000, PUESTA, "Viento", 0.8, 1, 1, 1);
        sistema.registrarPlantaHidraulica("Hidro2", "x", 500, PUESTA, "Agua", 0.1, 1, 1);
        sistema.registrarPlantaTermica("Termica2", "x", 1001, PUESTA, "Gas", 10, 1);
        assertEquals(300, sistema.capacidadMinimaCuota("Solar2"), 0.001);
        assertEquals(800, sistema.capacidadMinimaCuota("Eolica2"), 0.001);
        assertEquals(50, sistema.capacidadMinimaCuota("Hidro2"), 0.001);
        assertEquals(500.5, sistema.capacidadMinimaCuota("Termica2"), 0.001);
    }

    @Test
    public void capacidadMinimaDePlantaInexistenteFalla() {
        assertThrows(RuntimeException.class, () -> sistema.capacidadMinimaCuota("Nada"));
    }

    // ------------------------------------------------------------------
    // 3. Estado activa / inactiva
    // ------------------------------------------------------------------

    @Test
    public void estadoInicialEsActiva() {
        assertTrue(sistema.estaActiva("Solar1"));
        assertTrue(sistema.estaActiva("Eolica1"));
        assertTrue(sistema.estaActiva("Hidro1"));
        assertTrue(sistema.estaActiva("Termica1"));
    }

    @Test
    public void operacionesDeEstadoSobrePlantaInexistenteFallan() {
        assertThrows(RuntimeException.class, () -> sistema.estaActiva("Nada"));
        assertThrows(RuntimeException.class, () -> sistema.activarPlanta("Nada", h(DIA1, 10)));
        assertThrows(RuntimeException.class, () -> sistema.desactivarPlanta("Nada", h(DIA1, 10)));
    }

    @Test
    public void activarYDesactivarConFechaInvalidaFalla() {
        assertThrows(RuntimeException.class, () -> sistema.desactivarPlanta("Solar1", "manana"));
        assertThrows(RuntimeException.class, () -> sistema.desactivarPlanta("Solar1", null));
        assertThrows(RuntimeException.class, () -> sistema.activarPlanta("Solar1", "manana"));
        assertThrows(RuntimeException.class, () -> sistema.activarPlanta("Solar1", DIA1)); // fecha sin hora
    }

    @Test
    public void desactivarUnaPlantaNoAfectaAOtras() {
        sistema.desactivarPlanta("Solar1", h(DIA1, 9));
        assertFalse(sistema.estaActiva("Solar1"));
        assertTrue(sistema.estaActiva("Eolica1"));
        assertTrue(sistema.estaActiva("Hidro1"));
        assertTrue(sistema.estaActiva("Termica1"));
        // otra planta sigue pudiendo monitorear
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 10), 5, 0);
        assertEquals(5, sistema.totalGenerado("Hidro1"), 0.001);
    }

    @Test
    public void variasDesactivacionesYActivacionesAlternadas() {
        sistema.desactivarPlanta("Solar1", h(DIA1, 8));
        sistema.activarPlanta("Solar1", h(DIA1, 10));
        sistema.desactivarPlanta("Solar1", h(DIA1, 14));
        assertFalse(sistema.estaActiva("Solar1"));
        sistema.activarPlanta("Solar1", h(DIA1, 16));
        assertTrue(sistema.estaActiva("Solar1"));
    }

    // ------------------------------------------------------------------
    // 4. Registro de monitoreo
    // ------------------------------------------------------------------

    @Test
    public void monitoreoConDatosInvalidosFallaSinDejarRastros() {
        String t = h(DIA1, 5);
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Nada", t, 1, 0));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Solar1", null, 1, 0));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Solar1", "no-es-fecha", 1, 0));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Solar1", t, -1, 0));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Solar1", t, 1, -1));
        assertEquals(0, sistema.totalGenerado("Solar1"), 0.001);
        // la hora no quedo ocupada por los intentos fallidos
        sistema.registrarMonitoreo("Solar1", t, 1, 0);
        assertEquals(1, sistema.totalGenerado("Solar1"), 0.001);
    }

    @Test
    public void monitoreoRechazaValoresNegativosEspecificosDelTipo() {
        // CRITERIO: valores negativos de viento, CO2 o combustible son "inaceptables".
        assertThrows(RuntimeException.class,
            () -> sistema.registrarMonitoreo("Eolica1", h(DIA1, 1), 10, 0, -1));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarMonitoreo("Termica1", h(DIA1, 1), 10, 0, -1, 5));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarMonitoreo("Termica1", h(DIA1, 1), 10, 0, 5, -1));
    }

    @Test
    public void monitoreoRechazaMetodoQueNoCorrespondeAlTipoDePlanta() {
        String t = h(DIA1, 3);
        // eolica necesita velocidad de viento
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Eolica1", t, 1, 0));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Eolica1", t, 1, 0, 1, 1));
        // hidraulica y solar usan solo energia e incidencias
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Hidro1", t, 1, 0, 1));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Hidro1", t, 1, 0, 1, 1));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Solar1", t, 1, 0, 1, 1));
        // termica necesita CO2 y combustible
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Termica1", t, 1, 0));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Termica1", t, 1, 0, 1));
        // ninguno de los intentos dejo datos
        assertEquals(0, sistema.totalGenerado("Eolica1"), 0.001);
        assertEquals(0, sistema.totalGenerado("Hidro1"), 0.001);
        assertEquals(0, sistema.totalGenerado("Solar1"), 0.001);
        assertEquals(0, sistema.totalGenerado("Termica1"), 0.001);
    }

    @Test
    public void monitoreoValidoDeCadaTipoSeRegistra() {
        sistema.registrarMonitoreo("Solar1", h(DIA1, 1), 10, 0);
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 1), 20, 1, 7.5);
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 1), 30, 2);
        sistema.registrarMonitoreo("Termica1", h(DIA1, 1), 40, 3, 12.5, 4);
        assertEquals(10, sistema.totalGenerado("Solar1"), 0.001);
        assertEquals(20, sistema.totalGenerado("Eolica1"), 0.001);
        assertEquals(30, sistema.totalGenerado("Hidro1"), 0.001);
        assertEquals(40, sistema.totalGenerado("Termica1"), 0.001);
    }

    @Test
    public void mismaHoraEnDistintasPlantasOEnDistintosDiasEsValido() {
        sistema.registrarMonitoreo("Solar1", h(DIA1, 7), 1, 0);
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 7), 1, 0);
        sistema.registrarMonitoreo("Solar1", h(DIA2, 7), 1, 0);
        assertEquals(2, sistema.totalGenerado("Solar1"), 0.001);
        // pero repetir dia y hora en la misma planta falla
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Solar1", h(DIA1, 7), 9, 9));
        assertEquals(2, sistema.totalGenerado("Solar1"), 0.001);
    }

    @Test
    public void duplicadoTambienSeDetectaEnEolicaYTermica() {
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 7), 1, 0, 3);
        assertThrows(RuntimeException.class,
            () -> sistema.registrarMonitoreo("Eolica1", h(DIA1, 7), 1, 0, 9));
        sistema.registrarMonitoreo("Termica1", h(DIA1, 7), 1, 0, 3, 3);
        assertThrows(RuntimeException.class,
            () -> sistema.registrarMonitoreo("Termica1", h(DIA1, 7), 1, 0, 9, 9));
    }

    @Test
    public void monitoreoExactamenteEnLaPuestaEnMarchaEsValidoYAnteriorNo() {
        sistema.registrarPlantaSolar("Nueva", "x", 100, "2026-03-01T10:00:00", "Sol", 0.5, 1, 1, 12);
        assertThrows(RuntimeException.class,
            () -> sistema.registrarMonitoreo("Nueva", "2026-03-01T09:00:00", 1, 0));
        sistema.registrarMonitoreo("Nueva", "2026-03-01T10:00:00", 1, 0);
        assertEquals(1, sistema.totalGenerado("Nueva"), 0.001);
    }

    @Test
    public void monitoreoEnPlantaInactivaFallaYAlReactivarSePuedeMonitorear() {
        sistema.desactivarPlanta("Termica1", h(DIA1, 9));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarMonitoreo("Termica1", h(DIA1, 10), 300, 0, 1, 1));
        sistema.activarPlanta("Termica1", h(DIA1, 11));
        sistema.registrarMonitoreo("Termica1", h(DIA1, 12), 300, 0, 1, 1);
        assertEquals(300, sistema.totalGenerado("Termica1"), 0.001);
    }

    @Test
    public void monitoreoEnEolicaInactivaFalla() {
        sistema.desactivarPlanta("Eolica1", h(DIA1, 9));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarMonitoreo("Eolica1", h(DIA1, 10), 1, 0, 6));
        assertEquals(0, sistema.totalGenerado("Eolica1"), 0.001);
    }

    @Test
    public void monitoreoDentroDeUnPeriodoInactivoPasadoSeRechaza() {
        // CRITERIO (estricto): el enunciado dice que no deben existir registros en el periodo
        // en que la planta estuvo inactiva, y que cada planta conoce esos periodos.
        sistema.desactivarPlanta("Solar1", h(DIA1, 10));
        sistema.activarPlanta("Solar1", h(DIA1, 14));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Solar1", h(DIA1, 11), 1, 0));
        // antes y despues del periodo inactivo si se puede
        sistema.registrarMonitoreo("Solar1", h(DIA1, 9), 1, 0);
        sistema.registrarMonitoreo("Solar1", h(DIA1, 15), 1, 0);
        assertEquals(2, sistema.totalGenerado("Solar1"), 0.001);
    }

    // ------------------------------------------------------------------
    // 5. Total generado
    // ------------------------------------------------------------------

    @Test
    public void totalGeneradoEsCeroSinMonitoreosYFallaSiNoExiste() {
        assertEquals(0, sistema.totalGenerado("Solar1"), 0.0001);
        assertEquals(0, sistema.totalGenerado("Termica1"), 0.0001);
        assertThrows(RuntimeException.class, () -> sistema.totalGenerado("Nada"));
    }

    @Test
    public void totalGeneradoAcumulaDecimalesYNoMezclaPlantas() {
        sistema.registrarMonitoreo("Solar1", h(DIA1, 1), 10.5, 0);
        sistema.registrarMonitoreo("Solar1", h(DIA1, 2), 0.25, 0);
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 1), 1000, 0);
        assertEquals(10.75, sistema.totalGenerado("Solar1"), 0.0001);
        assertEquals(1000, sistema.totalGenerado("Hidro1"), 0.0001);
        assertEquals(0, sistema.totalGenerado("Eolica1"), 0.0001);
    }

    // ------------------------------------------------------------------
    // 6. Incidencias por hora
    // ------------------------------------------------------------------

    @Test
    public void incidenciasPorHoraSinMonitoreosDevuelve24Ceros() {
    	
        int[] r = sistema.incidenciasPorHora("Solar1");
        assertEquals(24, r.length);
        assertArrayEquals(new int[24], r);
        assertThrows(RuntimeException.class, () -> sistema.incidenciasPorHora("Nada"));
    }

    @Test
    public void incidenciasSeAcumulanPorHoraEntreDiasYSoloDeEsaPlanta() {
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 0), 1, 1);
        sistema.registrarMonitoreo("Hidro1", h(DIA2, 0), 1, 2);
        sistema.registrarMonitoreo("Hidro1", h(DIA3, 0), 1, 3);
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 23), 1, 5);
        sistema.registrarMonitoreo("Solar1", h(DIA1, 0), 1, 100);

        int[] esperado = new int[24];
        esperado[0] = 6;
        esperado[23] = 5;
        assertArrayEquals(esperado, sistema.incidenciasPorHora("Hidro1"));
    }

    @Test
    public void incidenciasIncluyeEolicaYTermica() {
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 6), 1, 4, 3);
        sistema.registrarMonitoreo("Termica1", h(DIA1, 6), 1, 7, 1, 1);
        assertEquals(4, sistema.incidenciasPorHora("Eolica1")[6]);
        assertEquals(7, sistema.incidenciasPorHora("Termica1")[6]);
    }

    @Test
    public void modificarElArregloDevueltoNoAfectaElEstadoInterno() {
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 3), 1, 2);
        int[] r = sistema.incidenciasPorHora("Hidro1");
        r[3] = 999;
        r[0] = 999;
        int[] otra = sistema.incidenciasPorHora("Hidro1");
        assertEquals(2, otra[3]);
        assertEquals(0, otra[0]);
    }

    // ------------------------------------------------------------------
    // 7. Histograma de cumplimiento
    // ------------------------------------------------------------------

    @Test
    public void histogramaSinMonitoreosDevuelve24Ceros() {
        int[] hist = sistema.histogramaCumplimiento("Solar1", DIA1, DIA3);
        assertEquals(24, hist.length);
        assertArrayEquals(new int[24], hist);
    }

    @Test
    public void histogramaValidaPlantaYFechas() {
        assertThrows(RuntimeException.class, () -> sistema.histogramaCumplimiento("Nada", DIA1, DIA2));
        assertThrows(RuntimeException.class, () -> sistema.histogramaCumplimiento("Solar1", "ayer", DIA2));
        assertThrows(RuntimeException.class, () -> sistema.histogramaCumplimiento("Solar1", DIA1, "hoy"));
        assertThrows(RuntimeException.class, () -> sistema.histogramaCumplimiento("Solar1", DIA1, h(DIA2, 0)));
        assertThrows(RuntimeException.class, () -> sistema.histogramaCumplimiento("Solar1", DIA3, DIA1));
    }

    @Test
    public void histogramaIncluyeAmbosExtremosYExcluyeLosDemasDias() {
        sistema.registrarMonitoreo("Solar1", h("2026-02-28", 8), 300, 0); // antes del rango
        sistema.registrarMonitoreo("Solar1", h(DIA1, 8), 300, 0);         // extremo inferior
        sistema.registrarMonitoreo("Solar1", h(DIA2, 8), 300, 0);         // medio
        sistema.registrarMonitoreo("Solar1", h(DIA3, 8), 300, 0);         // extremo superior
        sistema.registrarMonitoreo("Solar1", h("2026-03-04", 8), 300, 0); // despues del rango

        assertEquals(3, sistema.histogramaCumplimiento("Solar1", DIA1, DIA3)[8]);
        assertEquals(1, sistema.histogramaCumplimiento("Solar1", DIA1, DIA1)[8]);
        assertEquals(1, sistema.histogramaCumplimiento("Solar1", DIA3, DIA3)[8]);
        assertEquals(2, sistema.histogramaCumplimiento("Solar1", DIA2, DIA3)[8]);
    }

    @Test
    public void histogramaCuentaSoloLosDiasEnQueSeCumplio() {
        sistema.registrarMonitoreo("Solar1", h(DIA1, 12), 250, 0);    // justo la cuota: cumple
        sistema.registrarMonitoreo("Solar1", h(DIA2, 12), 249.99, 0); // por debajo: no cumple
        sistema.registrarMonitoreo("Solar1", h(DIA3, 12), 1000, 0);   // cumple

        int[] hist = sistema.histogramaCumplimiento("Solar1", DIA1, DIA3);
        assertEquals(2, hist[12]);
        int suma = 0;
        for (int v : hist) {
            suma += v;
        }
        assertEquals(2, suma);
    }

    @Test
    public void histogramaEolicaEHidraulicaEnElLimiteDeLaCuota() {
        // CRITERIO: igualar la cuota exacta cumple (igual que en la solar del test de la catedra).
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 4), 200, 0, 6);
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 5), 199.99, 0, 6);
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 4), 200, 0);
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 5), 199.99, 0);

        int[] e = sistema.histogramaCumplimiento("Eolica1", DIA1, DIA1);
        assertEquals(1, e[4]);
        assertEquals(0, e[5]);
        int[] hi = sistema.histogramaCumplimiento("Hidro1", DIA1, DIA1);
        assertEquals(1, hi[4]);
        assertEquals(0, hi[5]);
    }

    @Test
    public void histogramaTermicaExigeEnergiaYEmisionesMenoresAlObjetivo() {
        sistema.registrarMonitoreo("Termica1", h(DIA1, 1), 300, 0, 49.99, 1);  // cumple
        sistema.registrarMonitoreo("Termica1", h(DIA1, 2), 300, 0, 50, 1);     // CO2 igual al objetivo: no
        sistema.registrarMonitoreo("Termica1", h(DIA1, 3), 299.99, 0, 1, 1);   // energia insuficiente
        sistema.registrarMonitoreo("Termica1", h(DIA1, 4), 600, 0, 0, 0);      // sobrada y limpia: cumple
        sistema.registrarMonitoreo("Termica1", h(DIA1, 5), 600, 0, 500, 1);    // mucho CO2: no

        int[] hist = sistema.histogramaCumplimiento("Termica1", DIA1, DIA1);
        assertEquals(1, hist[1]);
        assertEquals(0, hist[2]);
        assertEquals(0, hist[3]);
        assertEquals(1, hist[4]);
        assertEquals(0, hist[5]);
    }

    @Test
    public void histogramaNoMezclaDatosDeOtrasPlantas() {
        sistema.registrarMonitoreo("Solar1", h(DIA1, 9), 300, 0);
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 9), 800, 0);
        int[] hist = sistema.histogramaCumplimiento("Solar1", DIA1, DIA1);
        assertEquals(1, hist[9]);
        assertEquals(1, Arrays.stream(hist).sum());
    }

    @Test
    public void histogramaAcumulaMuchosDiasParaLaMismaHora() {
        LocalDate d = LocalDate.parse("2026-01-01");
        for (int i = 0; i < 10; i++) {
            // los dias pares cumplen, los impares no
            sistema.registrarMonitoreo("Solar1", h(d.plusDays(i).toString(), 15), i % 2 == 0 ? 400 : 10, 0);
        }
        int[] hist = sistema.histogramaCumplimiento("Solar1", "2026-01-01", "2026-01-10");
        assertEquals(5, hist[15]);
        // subrango de 4 dias (dias 3 al 6 => indices 2..5 => pares: 2 y 4)
        assertEquals(2, sistema.histogramaCumplimiento("Solar1", "2026-01-03", "2026-01-06")[15]);
    }

    @Test(timeout = 15000)
    public void histogramaConDosAniosDeDatosPorHora() {
        LocalDate inicio = LocalDate.parse("2024-01-01");
        int dias = 730;
        for (int i = 0; i < dias; i++) {
            String dia = inicio.plusDays(i).toString();
            for (int hora = 0; hora < 24; hora++) {
                sistema.registrarMonitoreo("Solar1", h(dia, hora), 300, 0);
            }
        }
        LocalDate fin = inicio.plusDays(dias - 1);
        int[] hist = sistema.histogramaCumplimiento("Solar1", inicio.toString(), fin.toString());
        int[] esperado = new int[24];
        Arrays.fill(esperado, dias);
        assertArrayEquals(esperado, hist);

        int[] parcial = sistema.histogramaCumplimiento("Solar1",
            inicio.plusDays(10).toString(), inicio.plusDays(19).toString());
        Arrays.fill(esperado, 10);
        assertArrayEquals(esperado, parcial);
    }

    // ------------------------------------------------------------------
    // 8. Renovables por fuente
    // ------------------------------------------------------------------

    @Test
    public void renovablesExcluyeTermicasYEstaOrdenadoPorFuente() {
        sistema.registrarPlantaTermica("Termica2", "x", 10, PUESTA, "Carbon", 1, 1);
        sistema.registrarPlantaHidraulica("Hidro2", "x", 10, PUESTA, "Agua", 0.5, 1, 1);
        sistema.registrarPlantaHidraulica("Hidro3", "x", 10, PUESTA, "Agua", 0.5, 1, 1);

        Map<String, Integer> r = sistema.plantasRenovablesPorFuente();
        assertEquals(3, r.size());
        assertEquals(1, (int) r.get("Sol"));
        assertEquals(1, (int) r.get("Viento"));
        assertEquals(3, (int) r.get("Agua"));
        assertFalse(r.containsKey("Gas"));
        assertFalse(r.containsKey("Carbon"));
        // ordenadas por tipo de fuente
        assertEquals(Arrays.asList("Agua", "Sol", "Viento"), new ArrayList<>(r.keySet()));
    }

    @Test
    public void renovablesNoCuentaPlantasDesactivadasComoSiNoExistieran() {
        // CRITERIO: la consigna cuenta "plantas renovables por fuente", sin filtrar por estado.
        sistema.desactivarPlanta("Solar1", h(DIA1, 9));
        assertEquals(1, (int) sistema.plantasRenovablesPorFuente().get("Sol"));
    }

    @Test
    public void modificarElMapaDevueltoNoAfectaAlSistema() {
        Map<String, Integer> r = sistema.plantasRenovablesPorFuente();
        try {
            r.put("Sol", 99);
            r.clear();
        } catch (UnsupportedOperationException e) {
            // tambien es valido devolver un mapa inmodificable
        }
        Map<String, Integer> otra = sistema.plantasRenovablesPorFuente();
        assertEquals(3, otra.size());
        assertEquals(1, (int) otra.get("Sol"));
    }

    // ------------------------------------------------------------------
    // 9. Plantas que nunca cumplieron la cuota
    // ------------------------------------------------------------------

    @Test
    public void sinMonitoreosTodasNuncaCumplieronEnOrdenDeRegistro() {
        assertEquals(Arrays.asList("Solar1", "Eolica1", "Hidro1", "Termica1"),
            sistema.plantasQueNuncaCumplieronCuota());
    }

    @Test
    public void conUnSoloCumplimientoLaPlantaDejaDeEstarEnLaLista() {
        sistema.registrarMonitoreo("Solar1", h(DIA1, 1), 10, 0);   // no cumple
        sistema.registrarMonitoreo("Solar1", h(DIA1, 2), 250, 0);  // cumple
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 1), 10, 0);   // no cumple
        assertEquals(Arrays.asList("Eolica1", "Hidro1", "Termica1"),
            sistema.plantasQueNuncaCumplieronCuota());
    }

    @Test
    public void termicaQueEmiteDeMasNuncaCumple() {
        sistema.registrarMonitoreo("Termica1", h(DIA1, 1), 600, 0, 50, 1);
        sistema.registrarMonitoreo("Termica1", h(DIA1, 2), 600, 0, 1000, 1);
        assertTrue(sistema.plantasQueNuncaCumplieronCuota().contains("Termica1"));
        sistema.registrarMonitoreo("Termica1", h(DIA1, 3), 600, 0, 49, 1);
        assertFalse(sistema.plantasQueNuncaCumplieronCuota().contains("Termica1"));
    }

    @Test
    public void plantaQueCumplioYDespuesSeDesactivoNoEsDeLasQueNuncaCumplieron() {
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 1), 500, 0);
        sistema.desactivarPlanta("Hidro1", h(DIA1, 5));
        assertFalse(sistema.plantasQueNuncaCumplieronCuota().contains("Hidro1"));
    }

    @Test
    public void todasLasPlantasCumplenYLaListaQuedaVacia() {
        sistema.registrarMonitoreo("Solar1", h(DIA1, 1), 250, 0);
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 1), 200, 0, 5);
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 1), 200, 0);
        sistema.registrarMonitoreo("Termica1", h(DIA1, 1), 300, 0, 49, 1);
        assertTrue(sistema.plantasQueNuncaCumplieronCuota().isEmpty());
    }

    // ------------------------------------------------------------------
    // 10. Horas con poco viento (plantas eolicas)
    // ------------------------------------------------------------------

    @Test
    public void reporteDeVientoTieneEncabezadoYSoloListaPlantasEolicas() {
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 2), 10, 0, 1);
        sistema.registrarMonitoreo("Solar1", h(DIA1, 2), 10, 0);
        String r = sistema.horasSinVelocidadMinimaPorPlantaEolica();
        assertEquals("Plantas Eolicas: Horas con poco viento", r.trim().split("\\R")[0].trim());
        assertTrue(r.contains("Eolica1"));
        assertFalse(r.contains("Solar1"));
        assertFalse(r.contains("Hidro1"));
        assertFalse(r.contains("Termica1"));
    }

    @Test
    public void reporteDeVientoListaHorasSinRepetirYEnOrdenAscendente() {
        // velocidad minima de Eolica1 = 5
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 11), 1, 0, 3);
        sistema.registrarMonitoreo("Eolica1", h(DIA2, 11), 1, 0, 2); // misma hora, otro dia, tambien bajo
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 2), 1, 0, 1);
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 7), 1, 0, 0);
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 8), 1, 0, 9);  // alcanzo
        sistema.registrarMonitoreo("Eolica1", h(DIA2, 8), 1, 0, 1);  // otro dia bajo, pero ya alcanzo
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 9), 1, 0, 5);  // justo la minima: alcanzo

        List<Integer> horas = horasListadas(sistema.horasSinVelocidadMinimaPorPlantaEolica(), "Eolica1");
        assertEquals(Arrays.asList(2, 7, 11), horas);
    }

    @Test
    public void reporteDeVientoUsaDosDigitosParaLaHora() {
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 7), 1, 0, 1);
        String r = sistema.horasSinVelocidadMinimaPorPlantaEolica();
        boolean hay = false;
        for (String linea : r.split("\\R")) {
            if (linea.trim().startsWith("07:")) {
                hay = true;
                assertTrue(linea.contains("m/s"));
            }
        }
        assertTrue("falta la linea de la hora 07", hay);
    }

    @Test
    public void reporteDeVientoMuestraLaVelocidadMinimaDeLaPlanta() {
        String r = sistema.horasSinVelocidadMinimaPorPlantaEolica();
        // la planta (aun sin registros) o su linea, si aparece, debe indicar la velocidad minima en m/s
        for (String linea : r.split("\\R")) {
            if (linea.trim().startsWith("- Eolica1")) {
                assertTrue(linea.contains("5"));
                assertTrue(linea.contains("m/s"));
            }
        }
    }

    @Test
    public void reporteDeVientoConVariasEolicasRespetaOrdenYNoMezclaHoras() {
        sistema.registrarPlantaEolica("Eolica2", "x", 100, PUESTA, "Viento", 0.5, 2, 50, 8);
        // Eolica1 (min 5) y Eolica2 (min 8) a la misma hora con viento 7
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 3), 1, 0, 7);
        sistema.registrarMonitoreo("Eolica2", h(DIA1, 3), 1, 0, 7);
        // solo Eolica1 tiene viento bajo en la hora 4
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 4), 1, 0, 1);
        sistema.registrarMonitoreo("Eolica2", h(DIA1, 4), 1, 0, 20);

        String r = sistema.horasSinVelocidadMinimaPorPlantaEolica();
        assertTrue(r.indexOf("- Eolica1") >= 0);
        assertTrue(r.indexOf("- Eolica2") >= 0);
        assertTrue(r.indexOf("- Eolica1") < r.indexOf("- Eolica2"));
        assertEquals(Arrays.asList(4), horasListadas(r, "Eolica1"));
        assertEquals(Arrays.asList(3), horasListadas(r, "Eolica2"));
    }

    @Test
    public void reporteDeVientoDesaparecenLasHorasSiSeLimpiaElRegistro() {
        // un registro con energia 0 e incidencias 0 se elimina, y con el su hora de poco viento
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 4), 0, 0, 1);
        assertEquals(Arrays.asList(4),
            horasListadas(sistema.horasSinVelocidadMinimaPorPlantaEolica(), "Eolica1"));
        assertEquals(1, sistema.limpiarMonitoreosSinDatos());
        assertTrue(horasListadas(sistema.horasSinVelocidadMinimaPorPlantaEolica(), "Eolica1").isEmpty());
    }

    // ------------------------------------------------------------------
    // 11. Limpieza de monitoreos sin datos
    // ------------------------------------------------------------------

    @Test
    public void limpiarSinRegistrosParaBorrarDevuelveCero() {
        assertEquals(0, sistema.limpiarMonitoreosSinDatos());
        sistema.registrarMonitoreo("Solar1", h(DIA1, 1), 10, 0);
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 1), 0, 2);
        assertEquals(0, sistema.limpiarMonitoreosSinDatos());
        assertEquals(10, sistema.totalGenerado("Solar1"), 0.001);
        assertEquals(2, sistema.incidenciasPorHora("Hidro1")[1]);
    }

    @Test
    public void limpiarBorraDeTodasLasPlantasYSegundaVezDevuelveCero() {
        sistema.registrarMonitoreo("Solar1", h(DIA1, 1), 0, 0);
        sistema.registrarMonitoreo("Solar1", h(DIA1, 2), 0, 0);
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 1), 0, 0);
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 1), 0, 0, 4);
        sistema.registrarMonitoreo("Termica1", h(DIA1, 1), 0, 0, 7, 3); // CO2 y combustible no cuentan
        sistema.registrarMonitoreo("Termica1", h(DIA1, 2), 5, 0, 7, 3); // este se queda

        assertEquals(5, sistema.limpiarMonitoreosSinDatos());
        assertEquals(0, sistema.limpiarMonitoreosSinDatos());
        assertEquals(5, sistema.totalGenerado("Termica1"), 0.001);
    }

    @Test
    public void despuesDeLimpiarLaHoraQuedaLibreParaVolverARegistrar() {
        sistema.registrarMonitoreo("Solar1", h(DIA1, 6), 0, 0);
        assertEquals(1, sistema.limpiarMonitoreosSinDatos());
        sistema.registrarMonitoreo("Solar1", h(DIA1, 6), 80, 1);
        assertEquals(80, sistema.totalGenerado("Solar1"), 0.001);
        assertEquals(1, sistema.incidenciasPorHora("Solar1")[6]);
    }

    @Test
    public void limpiarNoTocaPlantasNiSuEstado() {
        sistema.registrarMonitoreo("Solar1", h(DIA1, 6), 0, 0);
        sistema.desactivarPlanta("Hidro1", h(DIA1, 1));
        sistema.limpiarMonitoreosSinDatos();
        assertEquals(4, sistema.consultarPlantas().size());
        assertFalse(sistema.estaActiva("Hidro1"));
        assertEquals(Collections.singletonList("Hidro1"), sistema.plantasInactivasEn(h(DIA1, 2)));
    }

    // ------------------------------------------------------------------
    // 12. Orden por energia generada
    // ------------------------------------------------------------------

    @Test
    public void ordenPorEnergiaSinMonitoreosListaTodasConCero() {
        List<String> r = sistema.plantasOrdenadasPorEnergiaGenerada();
        assertEquals(4, r.size());
        for (String s : r) {
            assertTrue(s, s.endsWith(": 0.0"));
        }
        assertEquals(new HashSet<>(Arrays.asList(
            "Solar1: 0.0", "Eolica1: 0.0", "Hidro1: 0.0", "Termica1: 0.0")), new HashSet<>(r));
    }

    @Test
    public void ordenPorEnergiaSumaTodosLosMonitoreosYSeReordena() {
        sistema.registrarMonitoreo("Solar1", h(DIA1, 1), 100, 0);
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 1), 60, 0);
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 2), 30, 0);
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 1), 20, 0, 6);
        List<String> antes = sistema.plantasOrdenadasPorEnergiaGenerada();
        assertEquals("Solar1: 100.0", antes.get(0));
        assertEquals("Hidro1: 90.0", antes.get(1));
        assertEquals("Eolica1: 20.0", antes.get(2));
        assertEquals("Termica1: 0.0", antes.get(3));

        // Hidro1 pasa a Solar1
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 3), 50, 0);
        List<String> despues = sistema.plantasOrdenadasPorEnergiaGenerada();
        assertEquals("Hidro1: 140.0", despues.get(0));
        assertEquals("Solar1: 100.0", despues.get(1));
    }

    @Test
    public void ordenPorEnergiaEsDescendenteConDecimales() {
        sistema.registrarMonitoreo("Solar1", h(DIA1, 1), 10.5, 0);
        sistema.registrarMonitoreo("Eolica1", h(DIA1, 1), 10.75, 0, 6);
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 1), 1000.5, 0);
        sistema.registrarMonitoreo("Termica1", h(DIA1, 1), 0.5, 0, 1, 1);
        assertEquals(Arrays.asList("Hidro1: 1000.5", "Eolica1: 10.75", "Solar1: 10.5", "Termica1: 0.5"),
            sistema.plantasOrdenadasPorEnergiaGenerada());
    }

    @Test
    public void ordenPorEnergiaSeActualizaDespuesDeLimpiar() {
        sistema.registrarMonitoreo("Solar1", h(DIA1, 1), 0, 0);
        sistema.registrarMonitoreo("Hidro1", h(DIA1, 1), 5, 0);
        sistema.limpiarMonitoreosSinDatos();
        assertEquals("Hidro1: 5.0", sistema.plantasOrdenadasPorEnergiaGenerada().get(0));
    }

    // ------------------------------------------------------------------
    // 13. Plantas inactivas en una fecha y hora
    // ------------------------------------------------------------------

    @Test
    public void inactivasEnUnaFechaConPeriodoCerradoYAbierto() {
        sistema.desactivarPlanta("Solar1", h(DIA1, 10));
        sistema.activarPlanta("Solar1", h(DIA1, 14));

        assertTrue(sistema.plantasInactivasEn(h(DIA1, 9)).isEmpty());
        assertEquals(Collections.singletonList("Solar1"), sistema.plantasInactivasEn(h(DIA1, 11)));
        assertEquals(Collections.singletonList("Solar1"), sistema.plantasInactivasEn(h(DIA1, 13)));
        assertTrue(sistema.plantasInactivasEn(h(DIA1, 15)).isEmpty());

        // segundo periodo, todavia abierto (sin reactivar)
        sistema.desactivarPlanta("Solar1", h(DIA1, 20));
        assertEquals(Collections.singletonList("Solar1"), sistema.plantasInactivasEn(h(DIA1, 21)));
        assertEquals(Collections.singletonList("Solar1"), sistema.plantasInactivasEn(h(DIA3, 0)));
        // el periodo viejo sigue recordado
        assertEquals(Collections.singletonList("Solar1"), sistema.plantasInactivasEn(h(DIA1, 12)));
        assertTrue(sistema.plantasInactivasEn(h(DIA1, 17)).isEmpty());
    }

    @Test
    public void inactivasEnVariasPlantasAlMismoTiempo() {
        sistema.desactivarPlanta("Solar1", h(DIA1, 10));
        sistema.desactivarPlanta("Termica1", h(DIA1, 11));
        sistema.desactivarPlanta("Hidro1", h(DIA1, 12));
        sistema.activarPlanta("Hidro1", h(DIA1, 13));

        List<String> en12 = sistema.plantasInactivasEn(h(DIA1, 12));
        assertEquals(3, en12.size());
        assertEquals(new HashSet<>(Arrays.asList("Solar1", "Termica1", "Hidro1")), new HashSet<>(en12));

        List<String> en14 = sistema.plantasInactivasEn(h(DIA1, 14));
        assertEquals(new HashSet<>(Arrays.asList("Solar1", "Termica1")), new HashSet<>(en14));
        assertEquals(2, en14.size());
    }

    @Test
    public void inactivasEnFechaInvalidaFalla() {
        assertThrows(RuntimeException.class, () -> sistema.plantasInactivasEn("hoy"));
        assertThrows(RuntimeException.class, () -> sistema.plantasInactivasEn(null));
        assertThrows(RuntimeException.class, () -> sistema.plantasInactivasEn(DIA1));
    }

    @Test
    public void inactivasEnNoDuplicaUnaPlantaConVariosPeriodos() {
        sistema.desactivarPlanta("Eolica1", h(DIA1, 1));
        sistema.activarPlanta("Eolica1", h(DIA1, 2));
        sistema.desactivarPlanta("Eolica1", h(DIA1, 3));
        sistema.activarPlanta("Eolica1", h(DIA1, 4));
        sistema.desactivarPlanta("Eolica1", h(DIA1, 5));
        assertEquals(Collections.singletonList("Eolica1"), sistema.plantasInactivasEn(h(DIA2, 0)));
    }

    // ------------------------------------------------------------------
    // toString del sistema
    // ------------------------------------------------------------------

    @Test
    public void toStringEsLegibleYMuestraLasPlantas() {
        String s = sistema.toString();
        assertNotNull(s);
        assertFalse(s.trim().isEmpty());
        assertFalse("falta implementar toString: " + s, s.startsWith(sistema.getClass().getName() + "@"));
        for (String nombre : Arrays.asList("Solar1", "Eolica1", "Hidro1", "Termica1")) {
            assertTrue("toString no menciona " + nombre, s.contains(nombre));
        }
    }

    @Test
    public void toStringCambiaConElEstadoInterno() {
        String antes = sistema.toString();
        sistema.registrarMonitoreo("Solar1", h(DIA1, 1), 123.5, 2);
        String conMonitoreo = sistema.toString();
        assertNotEquals(antes, conMonitoreo);
        sistema.desactivarPlanta("Hidro1", h(DIA1, 2));
        assertNotEquals(conMonitoreo, sistema.toString());
    }

    @Test
    public void toStringDeSistemaVacioNoFalla() {
        String s = new SmartGrids().toString();
        assertNotNull(s);
    }

    // ------------------------------------------------------------------
    // Escenario del codigo cliente
    // ------------------------------------------------------------------

    @Test
    public void escenarioDelCodigoCliente() {
        String inicio = "2026-10-08T00:00:00";
        String mon1 = "2026-10-08T10:00:00";
        String mon2 = "2026-10-08T11:00:00";
        String baja = "2026-10-08T12:00:00";

        ISmartGrids s = new SmartGrids();
        s.registrarPlantaSolar("Solar Norte", "Ruta 1", 1000, inicio, "Sol", 0.8, 1500, 12, 10);
        s.registrarPlantaEolica("Eolica Sur", "Ruta 2", 800, inicio, "Viento", 0.75, 4, 80, 5);
        assertEquals(Arrays.asList("Solar Norte (ACTIVA): " + inicio, "Eolica Sur (ACTIVA): " + inicio),
            s.consultarPlantas());

        s.registrarMonitoreo("Solar Norte", mon1, 0, 0);
        s.registrarMonitoreo("Solar Norte", mon2, 500, 1);
        s.registrarMonitoreo("Eolica Sur", mon1, 200, 0, 3);

        assertEquals(1, s.limpiarMonitoreosSinDatos());
        assertEquals(500, s.totalGenerado("Solar Norte"), 0.001);
        assertEquals(Arrays.asList("Solar Norte: 500.0", "Eolica Sur: 200.0"),
            s.plantasOrdenadasPorEnergiaGenerada());

        s.desactivarPlanta("Eolica Sur", baja);
        assertEquals(Collections.singletonList("Eolica Sur"), s.plantasInactivasEn("2026-10-08T13:00:00"));
        assertThrows(RuntimeException.class,
            () -> s.registrarMonitoreo("Eolica Sur", "2026-10-08T14:00:00", 100, 0, 3));
        assertFalse(s.toString().trim().isEmpty());
    }

    // ------------------------------------------------------------------
    // Escala (sanity check; no reemplaza el analisis de complejidad del informe)
    // ------------------------------------------------------------------

    @Test(timeout = 15000)
    public void muchasPlantasConEstadoYConsultas() {
        ISmartGrids grande = new SmartGrids();
        int n = 20000;
        for (int i = 0; i < n; i++) {
            grande.registrarPlantaHidraulica("H" + i, "x", 100, PUESTA, "Agua", 0.5, 1, 1);
        }
        for (int i = 0; i < n; i++) {
            grande.desactivarPlanta("H" + i, h(DIA1, 1));
        }
        for (int i = 0; i < n; i++) {
            assertFalse(grande.estaActiva("H" + i));
        }
        for (int i = 0; i < n; i += 2) {
            grande.activarPlanta("H" + i, h(DIA1, 2));
        }
        assertEquals(n, grande.consultarPlantas().size());
        assertEquals(n, (int) grande.plantasRenovablesPorFuente().get("Agua"));
        assertEquals(n / 2, grande.plantasInactivasEn(h(DIA1, 3)).size());
        assertEquals(n, grande.plantasQueNuncaCumplieronCuota().size());
    }
}