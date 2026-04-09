import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Lieu, ActiviteLieu } from '../models/trendy-places/lieu.model';

@Injectable({ providedIn: 'root' })
export class TrendyPlacesService {
  private BASE_URL = 'http://localhost:8089';

  constructor(private http: HttpClient) {}

  // Lieux
  getAllLieux(): Observable<Lieu[]> {
    return this.http.get<Lieu[]>(`${this.BASE_URL}/api/lieux`);
  }

  getLieuById(id: number): Observable<Lieu> {
    return this.http.get<Lieu>(`${this.BASE_URL}/api/lieux/${id}`);
  }

  createLieu(lieu: Lieu): Observable<Lieu> {
    return this.http.post<Lieu>(`${this.BASE_URL}/api/lieux`, lieu);
  }

  updateLieu(id: number, lieu: Lieu): Observable<Lieu> {
    return this.http.put<Lieu>(`${this.BASE_URL}/api/lieux/${id}`, lieu);
  }

  deleteLieu(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/api/lieux/${id}`);
  }

  // Activités
  getAllActivites(): Observable<ActiviteLieu[]> {
    return this.http.get<ActiviteLieu[]>(`${this.BASE_URL}/api/activites`);
  }

  getActivitesByLieu(lieuId: number): Observable<ActiviteLieu[]> {
    return this.http.get<ActiviteLieu[]>(`${this.BASE_URL}/api/activites/lieu/${lieuId}`);
  }

  createActivite(activite: ActiviteLieu, lieuId: number): Observable<ActiviteLieu> {
    return this.http.post<ActiviteLieu>(`${this.BASE_URL}/api/activites/lieu/${lieuId}`, activite);
  }

  updateActivite(id: number, activite: ActiviteLieu, lieuId: number): Observable<ActiviteLieu> {
    return this.http.put<ActiviteLieu>(`${this.BASE_URL}/api/activites/${id}/lieu/${lieuId}`, activite);
  }

  deleteActivite(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE_URL}/api/activites/${id}`);
  }

  // Réservations Activités
creerReservation(activiteId: number, userId: number, data: { nombrePersonnes: number }): Observable<any> {
  return this.http.post(`${this.BASE_URL}/api/reservations-activites/activite/${activiteId}/user/${userId}`, data);
}

getMesReservations(userId: number): Observable<any[]> {
  return this.http.get<any[]>(`${this.BASE_URL}/api/reservations-activites/user/${userId}`);
}

getAllReservationsAdmin(): Observable<any[]> {
  return this.http.get<any[]>(`${this.BASE_URL}/api/reservations-activites`);
}

updateStatutReservation(id: number, statut: string): Observable<any> {
  return this.http.patch(`${this.BASE_URL}/api/reservations-activites/${id}/statut`, { statut });
}

annulerReservation(id: number): Observable<void> {
  return this.http.delete<void>(`${this.BASE_URL}/api/reservations-activites/${id}`);
}

// Remplace simulerPaiement par :
payerReservation(reservationId: number, modePaiement: string, nombreTranches?: number): Observable<any> {
  return this.http.post(`${this.BASE_URL}/api/reservations-activites/${reservationId}/payer`, {
    modePaiement,
    nombreTranches: nombreTranches || null
  });
}

payerTranche(reservationId: number): Observable<any> {
  return this.http.post(`${this.BASE_URL}/api/reservations-activites/${reservationId}/payer-tranche`, {});
}

// Garde simulerPaiement pour compatibilité (pointe vers payerReservation)
simulerPaiement(reservationId: number): Observable<any> {
  return this.payerReservation(reservationId, 'TOTAL');
}

uploadImageLieu(file: File): Observable<any> {
  const formData = new FormData();
  formData.append('file', file);
  return this.http.post<any>(`${this.BASE_URL}/api/lieux/upload-image`, formData);
}

}