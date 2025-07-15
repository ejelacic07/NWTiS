package edu.unizg.foi.nwtis.podaci;


public record Partner(int id, String naziv, String vrstaKuhinje, String adresa, int mreznaVrata,
    int mreznaVrataKraj, float gpsSirina, float gpsDuzina, String sigurnosniKod, String adminKod) {


  public Partner partnerBezKodova() {
    return new Partner(id, naziv, vrstaKuhinje, adresa, mreznaVrata, mreznaVrataKraj, gpsSirina,
        gpsDuzina, "******", "******");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Partner))
      return false;
    Partner p = (Partner) o;
    return this.id == p.id;
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(id);
  }

}


