export type ReservationStatus = "Preparing" | "Ready" | "Checked in" | "Checked out";

export type Reservation = {
  id: number;
  guest: string;
  property: string;
  arrival: string;
  payout: string;
  status: ReservationStatus;
  updatedAt: string;
};

export type ReservationListResponse = {
  reservations: Reservation[];
};

export type UpdateReservationStatusPayload = {
  status: ReservationStatus;
};

export type UpdateReservationStatusResponse = {
  reservation: Reservation;
};
