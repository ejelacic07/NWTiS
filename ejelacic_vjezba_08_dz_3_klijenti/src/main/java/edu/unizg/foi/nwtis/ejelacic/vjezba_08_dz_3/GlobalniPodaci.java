package edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GlobalniPodaci {

  private int brojObracuna = 0;

  private final Map<Integer, Integer> brojOtvorenihNarudzbi = new ConcurrentHashMap<>();
  private final Map<Integer, Integer> brojRacuna = new ConcurrentHashMap<>();


  public synchronized int getBrojObracuna() {
    return brojObracuna;
  }

  public synchronized void setBrojObracuna(int brojObracuna) {
    this.brojObracuna = brojObracuna;
  }


  public synchronized void povecajBrojObracuna() {
    brojObracuna++;
  }


  public int getBrojOtvorenihNarudzbi(int partnerId) {
    return brojOtvorenihNarudzbi.getOrDefault(partnerId, 0);
  }

  public void povecajBrojOtvorenihNarudzbi(int partnerId) {
    brojOtvorenihNarudzbi.merge(partnerId, 1, Integer::sum);
  }

  public void smanjiBrojOtvorenihNarudzbi(int partnerId) {
    brojOtvorenihNarudzbi.computeIfPresent(partnerId, (k, v) -> (v > 0) ? v - 1 : 0);
  }

  public int getBrojRacuna(int partnerId) {
    return brojRacuna.getOrDefault(partnerId, 0);
  }

  public void povecajBrojRacuna(int partnerId) {
    brojRacuna.merge(partnerId, 1, Integer::sum);
  }


}
