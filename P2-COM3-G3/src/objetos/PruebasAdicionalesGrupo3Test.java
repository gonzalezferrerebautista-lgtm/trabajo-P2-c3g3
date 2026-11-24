package objetos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/** Envoltorio JUnit 4 de las mismas pruebas independientes del runner. */
public class PruebasAdicionalesGrupo3Test {
    @Test public void cuotasCuatroTipos() {
        PruebasAdicionalesGrupo3.cuotasCuatroTipos();
    }
    @Test public void noDuplicaMonitoreosEnMismaHora() { PruebasAdicionalesGrupo3.noDuplicaMonitoreosEnMismaHora(); }
    @Test public void historialBasicoDeInactividad() { PruebasAdicionalesGrupo3.historialBasicoDeInactividad(); }
    @Test public void histogramaFechasInclusivas() { PruebasAdicionalesGrupo3.histogramaFechasInclusivas(); }
    @Test public void fuentesRenovablesOrdenadas() { PruebasAdicionalesGrupo3.fuentesRenovablesOrdenadas(); }
    @Test public void vientoInsuficienteSoloSiNuncaSeAlcanzo() { PruebasAdicionalesGrupo3.vientoInsuficienteSoloSiNuncaSeAlcanzo(); }
    @Test public void limpiezaConIncidenciasDistintasDeCero() { PruebasAdicionalesGrupo3.limpiezaConIncidenciasDistintasDeCero(); }
    @Test public void rechazaTipoIncorrectoDeMonitoreo() { PruebasAdicionalesGrupo3.rechazaTipoIncorrectoDeMonitoreo(); }
    @Test public void noAdmiteMonitoreoSiActualmenteEstaInactiva() {
        PruebasAdicionalesGrupo3.noAdmiteMonitoreoSiActualmenteEstaInactiva();
    }
    @Test public void noPermiteDesactivarEolicaAntesDeMonitoreoExistente() { PruebasAdicionalesGrupo3.noPermiteDesactivarEolicaAntesDeMonitoreoExistente(); }
    @Test public void noPermiteDesactivarTermicaAntesDeMonitoreoExistente() { PruebasAdicionalesGrupo3.noPermiteDesactivarTermicaAntesDeMonitoreoExistente(); }
    @Test public void noAceptaMonitoreoAnteriorAInicioExacto() { PruebasAdicionalesGrupo3.noAceptaMonitoreoAnteriorAInicioExacto(); }
    @Test public void desactivacionConMinutosNoAfectaMinutosPrevios() { PruebasAdicionalesGrupo3.desactivacionConMinutosNoAfectaMinutosPrevios(); }
    @Test public void activacionConMinutosNoAfectaMinutosPrevios() { PruebasAdicionalesGrupo3.activacionConMinutosNoAfectaMinutosPrevios(); }
    @Test public void rechazaEnergiaNaN() { PruebasAdicionalesGrupo3.rechazaEnergiaNaN(); }
    @Test public void rechazaEnergiaInfinita() { PruebasAdicionalesGrupo3.rechazaEnergiaInfinita(); }
    @Test public void rechazaCapacidadInfinita() { PruebasAdicionalesGrupo3.rechazaCapacidadInfinita(); }
    @Test public void rechazaEficienciaNaN() { PruebasAdicionalesGrupo3.rechazaEficienciaNaN(); }
    @Test public void rechazaVelocidadVientoInfinita() { PruebasAdicionalesGrupo3.rechazaVelocidadVientoInfinita(); }
    @Test public void rechazaCO2Infinito() { PruebasAdicionalesGrupo3.rechazaCO2Infinito(); }
    @Test public void rechazaCombustibleNaN() { PruebasAdicionalesGrupo3.rechazaCombustibleNaN(); }
    
    @Test
    public void monitoreosNoDebenModificarseDesdeGetter() {
        Planta planta = new Solar(
            "Solar", "Ruta 1", 100, true,
            "2020-01-01T00:00:00",
            0.5, "Sol", 10, 10, 12
        );

        planta.registrarMonitoreo(
            "2026-03-01T10:00:00", 50, 0
        );

        try {
            planta.getMonitoreos().clear();
        } catch (UnsupportedOperationException e) {
            // Es correcto que el mapa no permita modificaciones.
        }

        assertEquals(1, planta.getMonitoreos().size());
        assertEquals(50.0, planta.totalGenerado(), 0.001);
    }
}
