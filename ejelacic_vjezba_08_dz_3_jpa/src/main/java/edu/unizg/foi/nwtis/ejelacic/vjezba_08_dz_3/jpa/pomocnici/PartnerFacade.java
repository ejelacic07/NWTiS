package edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3.jpa.pomocnici;


import java.util.List;
import edu.unizg.foi.nwtis.ejelacic.vjezba_08_dz_3.jpa.entiteti.Partneri;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class PartnerFacade {

  @PersistenceContext(unitName = "vjezba_08_dz_3_jpa")
  private EntityManager em;

  public List<Partneri> dohvatiSvePartnere() {
    return em.createNamedQuery("Partneri.findAll", Partneri.class).getResultList();
  }

  public Partneri dohvatiPartnerPoId(int id) {
    return em.find(Partneri.class, id);
  }

  public void dodajPartnera(Partneri partner) {
    em.persist(partner);
  }

  public void azurirajPartnera(Partneri partner) {
    em.merge(partner);
  }

  public void obrisiPartnera(Partneri partner) {
    em.remove(em.merge(partner));
  }
}
