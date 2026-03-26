"use client";

import { startTransition, useEffect, useMemo, useState } from "react";

import { apiUrl, authenticatedFetch, getToken, isAuthenticated, removeToken, setToken } from "@/lib/api";
import type {
  InboxListResponse,
  InquiryThread,
  SendReplyResponse,
} from "@/types/inbox";
import type {
  Listing,
  ListingListResponse,
  ListingStatus,
  UpdateListingStatusResponse,
} from "@/types/listing";
import type {
  Reservation,
  ReservationListResponse,
  ReservationStatus,
  UpdateReservationStatusResponse,
} from "@/types/reservation";

type Nav = "Home" | "Inbox" | "Reservations" | "Listings" | "Performance";
type Frame = "today" | "tomorrow" | "week";
type NotificationItem = { id: number; text: string };

const navs: Nav[] = ["Home", "Inbox", "Reservations", "Listings", "Performance"];
const frames: Frame[] = ["today", "tomorrow", "week"];
const reservationSteps: ReservationStatus[] = ["Preparing", "Ready", "Checked in", "Checked out"];
const box = "rounded-[24px] border border-[#ebe5df] bg-white p-6 shadow-[0_10px_30px_rgba(32,26,23,0.05)]";

const frameData = {
  today: {
    title: "What's happening today",
    stats: [["0", "Check-ins"], ["0", "Checkouts"], ["4", "Trips in progress"], ["4+", "Pending reviews"]],
    perf: ["64.0%", "100.0%", "3.8%"],
    your: [68, 52, 53, 45, 71],
    comp: [10, 8, 48, 86, 80],
    labels: ["07/27", "08/03", "08/10", "08/17", "08/24"],
  },
  tomorrow: {
    title: "What's happening tomorrow",
    stats: [["3", "Check-ins"], ["2", "Checkouts"], ["5", "Trips in progress"], ["2", "Pending reviews"]],
    perf: ["71.0%", "98.7%", "4.2%"],
    your: [60, 58, 66, 62, 78],
    comp: [22, 24, 43, 58, 63],
    labels: ["08/04", "08/05", "08/06", "08/07", "08/08"],
  },
  week: {
    title: "What's happening in the next 7 days",
    stats: [["12", "Check-ins"], ["9", "Checkouts"], ["18", "Trips in progress"], ["11", "Pending reviews"]],
    perf: ["78.0%", "99.4%", "4.9%"],
    your: [48, 55, 63, 71, 84],
    comp: [34, 36, 44, 57, 61],
    labels: ["Week 1", "Week 2", "Week 3", "Week 4", "Week 5"],
  },
} satisfies Record<Frame, { title: string; stats: [string, string][]; perf: string[]; your: number[]; comp: number[]; labels: string[] }>;

const initialNotifications: NotificationItem[] = [
  { id: 1, text: "Guest messages and listing updates are syncing normally." },
  { id: 2, text: "Review your weekday pricing before the next 7-day demand window." },
];

const recommendationItems = [
  "Let guests book instantly",
  "Refresh your first 5 listing photos",
  "Lower weekday pricing for September",
];

const path = (values: number[]) => values.map((value, index) => `${index ? "L" : "M"}${80 + index * 140} ${220 - value * 1.8}`).join(" ");

function Card({ value, label }: { value: string; label: string }) {
  return (
    <article className="rounded-[18px] border border-[#ebe5df] bg-white p-5 shadow-[0_10px_30px_rgba(32,26,23,0.05)]">
      <strong className="text-2xl font-semibold">{value}</strong>
      <p className="mt-2 text-sm text-[#6c625b]">{label}</p>
    </article>
  );
}

function buildReplyDraft(thread: Pick<InquiryThread, "guest" | "room">) {
  return `Hi ${thread.guest}, thanks for your message about ${thread.room}. I can share check-in details right away.`;
}

function buildTemplateReply(thread: Pick<InquiryThread, "guest">) {
  return `Hello ${thread.guest}, I have attached parking, check-in, and house rule details for you.`;
}

function replaceThread(threads: InquiryThread[], updatedThread: InquiryThread) {
  return threads.map((thread) => (thread.id === updatedThread.id ? updatedThread : thread));
}

function replaceReservation(reservations: Reservation[], updatedReservation: Reservation) {
  return reservations.map((reservation) => (reservation.id === updatedReservation.id ? updatedReservation : reservation));
}

function replaceListing(listings: Listing[], updatedListing: Listing) {
  return listings.map((listing) => (listing.id === updatedListing.id ? updatedListing : listing));
}

function getNextReservationStatus(status: ReservationStatus) {
  return reservationSteps[(reservationSteps.indexOf(status) + 1) % reservationSteps.length];
}

function getNextListingStatus(status: ListingStatus) {
  return status === "Published" ? "Draft" : "Published";
}

function getErrorMessage(error: unknown) {
  return error instanceof Error ? error.message : "Unexpected request failure.";
}

async function handleLogin(email: string, password: string, onSuccess: () => void, onError: (message: string) => void) {
  try {
    const response = await fetch(apiUrl("/api/auth/login"), {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
    if (!response.ok) {
      const errorPayload = (await response.json().catch(() => null)) as { error?: string } | null;
      throw new Error(errorPayload?.error ?? "Login failed.");
    }
    const data = (await response.json()) as { accessToken: string };
    setToken(data.accessToken);
    onSuccess();
  } catch (error: unknown) {
    onError(getErrorMessage(error));
  }
}

async function loadInboxThreads(signal?: AbortSignal) {
  const response = await authenticatedFetch("/api/inbox", { cache: "no-store", signal });
  if (!response.ok) {
    if (response.status === 401) throw new Error("UNAUTHORIZED");
    throw new Error("Failed to load inbox threads.");
  }
  const data = (await response.json()) as InboxListResponse;
  return data.threads;
}

async function loadReservations(signal?: AbortSignal) {
  const response = await authenticatedFetch("/api/reservations", { cache: "no-store", signal });
  if (!response.ok) {
    if (response.status === 401) throw new Error("UNAUTHORIZED");
    throw new Error("Failed to load reservations.");
  }
  const data = (await response.json()) as ReservationListResponse;
  return data.reservations;
}

async function loadListings(signal?: AbortSignal) {
  const response = await authenticatedFetch("/api/listings", { cache: "no-store", signal });
  if (!response.ok) {
    if (response.status === 401) throw new Error("UNAUTHORIZED");
    throw new Error("Failed to load listings.");
  }
  const data = (await response.json()) as ListingListResponse;
  return data.listings;
}

export default function Page() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [loginEmail, setLoginEmail] = useState("host@airnest.local");
  const [loginPassword, setLoginPassword] = useState("host1234!");
  const [isLoggingIn, setIsLoggingIn] = useState(false);
  const [loginError, setLoginError] = useState("");
  const [nav, setNav] = useState<Nav>("Home");
  const [homeFrame, setHomeFrame] = useState<Frame>("today");
  const [perfFrame, setPerfFrame] = useState<Frame>("week");
  const [requests, setRequests] = useState<InquiryThread[]>([]);
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [listings, setListings] = useState<Listing[]>([]);
  const [selectedRequestId, setSelectedRequestId] = useState<number | null>(null);
  const [selectedListingId, setSelectedListingId] = useState<number | null>(null);
  const [replyDraft, setReplyDraft] = useState("Loading conversation...");
  const [isInboxLoading, setIsInboxLoading] = useState(true);
  const [inboxError, setInboxError] = useState("");
  const [isSendingReply, setIsSendingReply] = useState(false);
  const [isReservationsLoading, setIsReservationsLoading] = useState(true);
  const [reservationsError, setReservationsError] = useState("");
  const [updatingReservationId, setUpdatingReservationId] = useState<number | null>(null);
  const [isListingsLoading, setIsListingsLoading] = useState(true);
  const [listingsError, setListingsError] = useState("");
  const [updatingListingId, setUpdatingListingId] = useState<number | null>(null);
  const [notifications, setNotifications] = useState(initialNotifications);
  const [toDos, setToDos] = useState<string[]>([]);
  const [profileOpen, setProfileOpen] = useState(false);
  const [toast, setToast] = useState("");

  const selectedRequest = useMemo(() => requests.find((thread) => thread.id === selectedRequestId) ?? null, [requests, selectedRequestId]);
  const selectedListing = useMemo(() => listings.find((listing) => listing.id === selectedListingId) ?? listings[0] ?? null, [listings, selectedListingId]);
  const home = frameData[homeFrame];
  const perf = frameData[perfFrame];
  const repliedCount = requests.filter((thread) => thread.status === "Replied").length;
  const responseRate = requests.length === 0 ? 0 : Math.round((repliedCount / requests.length) * 100);

  useEffect(() => {
    // 클라이언트 사이드에서만 인증 상태 확인
    setIsLoggedIn(isAuthenticated());
  }, []);

  useEffect(() => {
    if (!isLoggedIn) return;
    const controller = new AbortController();
    setIsInboxLoading(true);
    setInboxError("");

    loadInboxThreads(controller.signal)
      .then((threads) => {
        startTransition(() => {
          setRequests(threads);
          const firstThread = threads[0] ?? null;
          setSelectedRequestId(firstThread?.id ?? null);
          setReplyDraft(firstThread ? buildReplyDraft(firstThread) : "No guest conversations yet.");
        });
      })
      .catch((error: unknown) => {
        if (!controller.signal.aborted) {
          if (getErrorMessage(error) === "UNAUTHORIZED") {
            removeToken();
            setIsLoggedIn(false);
            return;
          }
          setInboxError(getErrorMessage(error));
          setReplyDraft("Inbox is unavailable right now.");
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setIsInboxLoading(false);
      });

    return () => controller.abort();
  }, [isLoggedIn]);

  useEffect(() => {
    if (!isLoggedIn) return;
    const controller = new AbortController();
    setIsReservationsLoading(true);
    setReservationsError("");

    loadReservations(controller.signal)
      .then((items) => setReservations(items))
      .catch((error: unknown) => {
        if (!controller.signal.aborted) {
          if (getErrorMessage(error) === "UNAUTHORIZED") {
            removeToken();
            setIsLoggedIn(false);
            return;
          }
          setReservationsError(getErrorMessage(error));
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setIsReservationsLoading(false);
      });

    return () => controller.abort();
  }, [isLoggedIn]);

  useEffect(() => {
    if (!isLoggedIn) return;
    const controller = new AbortController();
    setIsListingsLoading(true);
    setListingsError("");

    loadListings(controller.signal)
      .then((items) => {
        setListings(items);
        setSelectedListingId((current) => current ?? items[0]?.id ?? null);
      })
      .catch((error: unknown) => {
        if (!controller.signal.aborted) {
          if (getErrorMessage(error) === "UNAUTHORIZED") {
            removeToken();
            setIsLoggedIn(false);
            return;
          }
          setListingsError(getErrorMessage(error));
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) setIsListingsLoading(false);
      });

    return () => controller.abort();
  }, [isLoggedIn]);

  useEffect(() => {
    if (!toast) return;
    const timeoutId = window.setTimeout(() => setToast(""), 2400);
    return () => window.clearTimeout(timeoutId);
  }, [toast]);

  const ping = (message: string) => setToast(message);
  const note = (text: string) => setNotifications((current) => [{ id: Date.now(), text }, ...current]);
  const todo = (task: string) => setToDos((current) => (current.includes(task) ? current : [...current, task]));
  const openNav = (next: Nav) => {
    setNav(next);
    if (next === "Performance") setPerfFrame("week");
    ping(`${next} view activated.`);
  };
  const openThread = (thread: InquiryThread, draft?: string) => {
    setNav("Inbox");
    setSelectedRequestId(thread.id);
    setReplyDraft(draft ?? buildReplyDraft(thread));
    ping("Conversation loaded.");
  };

  async function handleSendReply() {
    if (!selectedRequest || isSendingReply) return;
    const message = replyDraft.trim();
    if (!message) return ping("Reply message cannot be empty.");

    setIsSendingReply(true);
    try {
      const response = await authenticatedFetch(`/api/inbox/${selectedRequest.id}/reply`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ message }),
      });
      if (!response.ok) {
        if (response.status === 401) {
          removeToken();
          window.location.reload();
          return;
        }
        const errorPayload = (await response.json().catch(() => null)) as { error?: string } | null;
        throw new Error(errorPayload?.error ?? "Failed to save the reply.");
      }
      const data = (await response.json()) as SendReplyResponse;
      setRequests((current) => replaceThread(current, data.thread));
      setReplyDraft(`Hi ${data.thread.guest}, happy to help with anything else you need.`);
      note(`Reply sent to ${data.thread.guest}.`);
      ping("Guest reply sent.");
    } catch (error: unknown) {
      ping(getErrorMessage(error));
    } finally {
      setIsSendingReply(false);
    }
  }

  async function handleAdvanceReservation(reservation: Reservation) {
    if (updatingReservationId) return;
    const nextStatus = getNextReservationStatus(reservation.status);
    setUpdatingReservationId(reservation.id);
    try {
      const response = await authenticatedFetch(`/api/reservations/${reservation.id}/status`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status: nextStatus }),
      });
      if (!response.ok) {
        if (response.status === 401) {
          removeToken();
          window.location.reload();
          return;
        }
        const errorPayload = (await response.json().catch(() => null)) as { error?: string } | null;
        throw new Error(errorPayload?.error ?? "Failed to update reservation status.");
      }
      const data = (await response.json()) as UpdateReservationStatusResponse;
      setReservations((current) => replaceReservation(current, data.reservation));
      note(`${data.reservation.guest} reservation updated.`);
      ping("Reservation status updated.");
    } catch (error: unknown) {
      ping(getErrorMessage(error));
    } finally {
      setUpdatingReservationId(null);
    }
  }

  async function handleToggleListingStatus(listing: Listing) {
    if (updatingListingId) return;
    const nextStatus = getNextListingStatus(listing.status);
    setUpdatingListingId(listing.id);
    try {
      const response = await authenticatedFetch(`/api/listings/${listing.id}/status`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status: nextStatus }),
      });
      if (!response.ok) {
        if (response.status === 401) {
          removeToken();
          window.location.reload();
          return;
        }
        const errorPayload = (await response.json().catch(() => null)) as { error?: string } | null;
        throw new Error(errorPayload?.error ?? "Failed to update listing status.");
      }
      const data = (await response.json()) as UpdateListingStatusResponse;
      setListings((current) => replaceListing(current, data.listing));
      setSelectedListingId(data.listing.id);
      note(`${data.listing.name} status updated.`);
      ping("Listing status updated.");
    } catch (error: unknown) {
      ping(getErrorMessage(error));
    } finally {
      setUpdatingListingId(null);
    }
  }

  const inboxList = isInboxLoading ? (
    <div className="mt-6 space-y-3">{Array.from({ length: 3 }).map((_, index) => <div key={index} className="rounded-[18px] border border-[#ebe5df] bg-[#fbf9f6] px-4 py-5 text-sm text-[#8b8179]">Loading conversation...</div>)}</div>
  ) : inboxError ? (
    <div className="mt-6 rounded-[18px] border border-[#e7cdb6] bg-[#fff6ef] px-4 py-4 text-sm text-[#7a5a3c]">{inboxError}</div>
  ) : requests.length === 0 ? (
    <p className="mt-6 text-sm text-[#6c625b]">No guest conversations yet.</p>
  ) : (
    <div className="mt-6 space-y-3">{requests.map((thread) => <button key={thread.id} type="button" onClick={() => openThread(thread)} className={`w-full rounded-[18px] border px-4 py-4 text-left ${selectedRequestId === thread.id ? "border-[#0f766e] bg-[#eef8f6]" : "border-[#ebe5df] bg-[#fbf9f6]"}`}><div className="flex items-start justify-between gap-4"><div><strong className="block">{thread.title}</strong><p className="mt-2 text-sm text-[#6c625b]">{thread.stay}</p><p className="text-sm text-[#6c625b]">{thread.room}</p></div><span className={`rounded-full px-2 py-1 text-xs ${thread.status === "Replied" ? "bg-[#dceee4] text-[#2e6b4d]" : "bg-[#f2e9de] text-[#7c634d]"}`}>{thread.status}</span></div></button>)}</div>
  );

  const reservationsList = isReservationsLoading ? (
    <div className="grid gap-4 xl:grid-cols-3">{Array.from({ length: 3 }).map((_, index) => <section key={index} className={box}><p className="text-sm text-[#8b8179]">Loading reservation...</p></section>)}</div>
  ) : reservationsError ? (
    <div className="rounded-[18px] border border-[#e7cdb6] bg-[#fff6ef] px-4 py-4 text-sm text-[#7a5a3c]">{reservationsError}</div>
  ) : (
    <div className="grid gap-4 xl:grid-cols-3">{reservations.map((reservation) => <section key={reservation.id} className={box}><h3 className="text-2xl font-semibold">{reservation.guest}</h3><p className="mt-2 text-sm text-[#6c625b]">{reservation.property}</p><div className="mt-5 space-y-3 text-sm"><div className="rounded-[16px] bg-[#f6f2ec] p-3">Arrival: {reservation.arrival}</div><div className="rounded-[16px] bg-[#f6f2ec] p-3">Payout: {reservation.payout}</div><div className="rounded-[16px] border border-[#ebe5df] p-3">Status: {reservation.status}</div></div><div className="mt-5 flex flex-wrap gap-3"><button type="button" onClick={() => handleAdvanceReservation(reservation)} disabled={updatingReservationId === reservation.id} className="rounded-md bg-[#0f766e] px-4 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:bg-[#77b8b1]">{updatingReservationId === reservation.id ? "Saving..." : "Advance status"}</button><button type="button" onClick={() => { const thread = requests.find((entry) => entry.guest === reservation.guest); if (thread) { openThread(thread, `Hi ${reservation.guest}, I am checking your reservation details now.`); } else { ping("No matching guest conversation found."); } }} className="rounded-md border border-[#e0d7cf] px-4 py-2 text-sm font-medium text-[#4e4641]">Message guest</button></div></section>)}</div>
  );

  const listingsList = isListingsLoading ? (
    <div className="mt-6 space-y-3">{Array.from({ length: 3 }).map((_, index) => <div key={index} className="rounded-[20px] border border-[#ebe5df] bg-[#fbf9f6] px-4 py-5 text-sm text-[#8b8179]">Loading listing...</div>)}</div>
  ) : listingsError ? (
    <div className="mt-6 rounded-[18px] border border-[#e7cdb6] bg-[#fff6ef] px-4 py-4 text-sm text-[#7a5a3c]">{listingsError}</div>
  ) : (
    <div className="mt-6 space-y-3">{listings.map((listing) => <button key={listing.id} type="button" onClick={() => { setSelectedListingId(listing.id); ping(`${listing.name} selected.`); }} className={`w-full rounded-[20px] border p-4 text-left ${selectedListingId === listing.id ? "border-[#0f766e] bg-[#eef8f6]" : "border-[#ebe5df] bg-[#fbf9f6]"}`}><div className="flex items-start justify-between gap-4"><div><strong className="block">{listing.name}</strong><p className="mt-2 text-sm text-[#6c625b]">{listing.location} / {listing.price}</p></div><span className="rounded-full bg-[#f2e9de] px-3 py-1 text-xs font-medium text-[#7c634d]">{listing.status}</span></div></button>)}</div>
  );

  const handleLoginSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setIsLoggingIn(true);
    setLoginError("");
    await handleLogin(
      loginEmail,
      loginPassword,
      () => {
        setIsLoggingIn(false);
        setIsLoggedIn(true);
      },
      (message) => {
        setIsLoggingIn(false);
        setLoginError(message);
      }
    );
  };

  const handleLogout = () => {
    removeToken();
    setIsLoggedIn(false);
  };

  if (!isLoggedIn) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-[#fbf9f6] text-[#1f1a17]">
        <div className="w-full max-w-md">
          <section className={`${box} mx-4`}>
            <div className="mb-6 text-center">
              <div className="mx-auto mb-4 grid h-16 w-16 place-items-center rounded-full bg-[#0f766e] text-2xl font-semibold text-white">A</div>
              <h1 className="text-3xl font-semibold">Host dashboard</h1>
              <p className="mt-2 text-sm text-[#6c625b]">Sign in to manage your listings and reservations</p>
            </div>
            <form onSubmit={handleLoginSubmit} className="space-y-4">
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-[#4e4641]">Email</label>
                <input
                  id="email"
                  type="email"
                  value={loginEmail}
                  onChange={(e) => setLoginEmail(e.target.value)}
                  required
                  className="mt-2 w-full rounded-[12px] border border-[#e0d7cf] px-4 py-3 text-sm outline-none focus:border-[#0f766e]"
                />
              </div>
              <div>
                <label htmlFor="password" className="block text-sm font-medium text-[#4e4641]">Password</label>
                <input
                  id="password"
                  type="password"
                  value={loginPassword}
                  onChange={(e) => setLoginPassword(e.target.value)}
                  required
                  className="mt-2 w-full rounded-[12px] border border-[#e0d7cf] px-4 py-3 text-sm outline-none focus:border-[#0f766e]"
                />
              </div>
              {loginError ? (
                <div className="rounded-[12px] border border-[#e7cdb6] bg-[#fff6ef] px-4 py-3 text-sm text-[#7a5a3c]">
                  {loginError}
                </div>
              ) : null}
              <button
                type="submit"
                disabled={isLoggingIn}
                className="w-full rounded-[12px] bg-[#0f766e] px-4 py-3 text-sm font-medium text-white disabled:cursor-not-allowed disabled:bg-[#77b8b1]"
              >
                {isLoggingIn ? "Signing in..." : "Sign in"}
              </button>
            </form>
            <p className="mt-6 text-center text-xs text-[#8b8179]">
              Local development account: host@airnest.local / host1234!
            </p>
          </section>
        </div>
      </main>
    );
  }

  const main = (() => {
    if (nav === "Inbox") {
      return <div className="grid gap-6 xl:grid-cols-[0.95fr_1.05fr]"><section className={box}><h2 className="text-[2rem] font-semibold">Inbox</h2>{inboxList}</section><section className={box}><h3 className="text-2xl font-semibold">Reply composer</h3>{selectedRequest ? <><div className="mt-6 rounded-[18px] bg-[#f6f2ec] p-4"><strong className="block">{selectedRequest.title}</strong><p className="mt-2 text-sm text-[#6c625b]">{selectedRequest.stay}</p><p className="text-sm text-[#6c625b]">{selectedRequest.room}</p></div><textarea value={replyDraft} onChange={(event) => setReplyDraft(event.target.value)} rows={7} className="mt-4 w-full rounded-[18px] border border-[#e0d7cf] px-4 py-3 text-sm outline-none" /><div className="mt-4 flex gap-3"><button type="button" onClick={handleSendReply} disabled={isSendingReply} className="rounded-md bg-[#0f766e] px-4 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:bg-[#77b8b1]">{isSendingReply ? "Saving..." : "Send reply"}</button><button type="button" onClick={() => setReplyDraft(buildTemplateReply(selectedRequest))} className="rounded-md border border-[#e0d7cf] px-4 py-2 text-sm font-medium text-[#4e4641]">Use template</button></div></> : <p className="mt-6 text-sm text-[#6c625b]">Select a guest conversation to start replying.</p>}</section></div>;
    }

    if (nav === "Reservations") {
      return <div className="space-y-8"><div><h2 className="text-[2rem] font-semibold">Reservations</h2><p className="mt-2 text-sm text-[#6c625b]">Move each stay through preparation, arrival, and checkout states.</p></div>{reservationsList}</div>;
    }

    if (nav === "Listings") {
      return <div className="grid gap-6 xl:grid-cols-[1fr_0.82fr]"><section className={box}><h2 className="text-[2rem] font-semibold">Listings</h2>{listingsList}</section><section className={box}><h3 className="text-2xl font-semibold">Listing controls</h3>{selectedListing ? <><div className="mt-6 rounded-[18px] bg-[#f6f2ec] p-4"><strong className="block text-lg">{selectedListing.name}</strong><p className="mt-2 text-sm text-[#6c625b]">{selectedListing.location} / {selectedListing.price}</p></div><div className="mt-5 grid gap-3"><button type="button" onClick={() => handleToggleListingStatus(selectedListing)} disabled={updatingListingId === selectedListing.id} className="rounded-md bg-[#0f766e] px-4 py-3 text-sm font-medium text-white disabled:cursor-not-allowed disabled:bg-[#77b8b1]">{updatingListingId === selectedListing.id ? "Saving..." : selectedListing.status === "Published" ? "Unpublish listing" : "Publish listing"}</button><button type="button" onClick={() => { todo(`Refresh photos for ${selectedListing.name}`); ping("Listing optimization added to your to-dos."); }} className="rounded-md border border-[#e0d7cf] px-4 py-3 text-sm font-medium text-[#4e4641]">Add photo refresh task</button><button type="button" onClick={() => { todo(`Review pricing for ${selectedListing.name}`); ping("Listing pricing review added to your to-dos."); }} className="rounded-md border border-[#e0d7cf] px-4 py-3 text-sm font-medium text-[#4e4641]">Add pricing review</button></div></> : <p className="mt-6 text-sm text-[#6c625b]">Select a listing to review details.</p>}</section></div>;
    }

    if (nav === "Performance") {
      return <div className="space-y-10"><div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between"><div><h2 className="text-[2rem] font-semibold">Performance</h2><p className="mt-2 text-sm text-[#6c625b]">Dedicated performance screen for occupancy, ratings, and conversion trends.</p></div><div className="flex gap-2 text-xs uppercase tracking-[0.14em] text-[#8b8179]">{frames.map((frame) => <button key={frame} type="button" onClick={() => setPerfFrame(frame)} className={`rounded-full px-3 py-1 ${perfFrame === frame ? "bg-[#0f766e] text-white" : ""}`}>{frame === "today" ? "Today" : frame === "tomorrow" ? "Tomorrow" : "Next 7 days"}</button>)}</div></div><div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{home.stats.map(([value, label]) => <Card key={label} value={value} label={label} />)}</div><section className={box}><h3 className="text-2xl font-semibold">Performance graph</h3><div className="mt-6 grid gap-6 sm:grid-cols-3"><div><strong className="text-2xl font-semibold">{perf.perf[0]}</strong><p className="mt-1 text-sm text-[#6c625b]">Occupancy rate</p></div><div><strong className="text-2xl font-semibold">{perf.perf[1]}</strong><p className="mt-1 text-sm text-[#6c625b]">5-star ratings</p></div><div><strong className="text-2xl font-semibold">{perf.perf[2]}</strong><p className="mt-1 text-sm text-[#6c625b]">Booking conversion rate</p></div></div><div className="mt-8"><svg viewBox="0 0 760 260" className="h-auto w-full" aria-hidden="true"><g stroke="#e8e1d8" strokeWidth="1"><line x1="80" y1="20" x2="80" y2="220" /><line x1="220" y1="20" x2="220" y2="220" /><line x1="360" y1="20" x2="360" y2="220" /><line x1="500" y1="20" x2="500" y2="220" /><line x1="640" y1="20" x2="640" y2="220" /></g><path d={path(perf.your)} fill="none" stroke="#111111" strokeWidth="4" strokeLinecap="round" /><path d={path(perf.comp)} fill="none" stroke="#a8a29e" strokeWidth="4" strokeDasharray="6 6" strokeLinecap="round" /></svg></div></section></div>;
    }

    return <div className="space-y-10"><div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between"><div><h2 className="text-[2rem] font-semibold">{home.title}</h2><p className="mt-2 text-sm text-[#6c625b]">A simplified host overview for reservations, performance, and actions.</p></div><div className="flex gap-2 text-xs uppercase tracking-[0.14em] text-[#8b8179]">{frames.map((frame) => <button key={frame} type="button" onClick={() => setHomeFrame(frame)} className={`rounded-full px-3 py-1 ${homeFrame === frame ? "bg-[#0f766e] text-white" : ""}`}>{frame === "today" ? "Today" : frame === "tomorrow" ? "Tomorrow" : "Next 7 days"}</button>)}</div></div><div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{home.stats.map(([value, label]) => <Card key={label} value={value} label={label} />)}</div><section className={box}><div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between"><div><h3 className="text-2xl font-semibold">Performance</h3><p className="mt-2 text-sm text-[#6c625b]">Track occupancy, quality, and conversion trends.</p></div><button type="button" onClick={() => { setNav("Performance"); setPerfFrame(homeFrame); ping("Performance view opened."); }} className="rounded-full border border-[#e0d7cf] px-4 py-2 text-sm font-medium text-[#4e4641]">Open full performance</button></div><div className="mt-6 grid gap-6 sm:grid-cols-3"><div><strong className="text-2xl font-semibold">{home.perf[0]}</strong><p className="mt-1 text-sm text-[#6c625b]">Occupancy rate</p></div><div><strong className="text-2xl font-semibold">{home.perf[1]}</strong><p className="mt-1 text-sm text-[#6c625b]">5-star ratings</p></div><div><strong className="text-2xl font-semibold">{home.perf[2]}</strong><p className="mt-1 text-sm text-[#6c625b]">Booking conversion rate</p></div></div><div className="mt-8"><svg viewBox="0 0 760 260" className="h-auto w-full" aria-hidden="true"><g stroke="#e8e1d8" strokeWidth="1"><line x1="80" y1="20" x2="80" y2="220" /><line x1="220" y1="20" x2="220" y2="220" /><line x1="360" y1="20" x2="360" y2="220" /><line x1="500" y1="20" x2="500" y2="220" /><line x1="640" y1="20" x2="640" y2="220" /></g><path d={path(home.your)} fill="none" stroke="#111111" strokeWidth="4" strokeLinecap="round" /><path d={path(home.comp)} fill="none" stroke="#a8a29e" strokeWidth="4" strokeDasharray="6 6" strokeLinecap="round" /></svg></div></section><section className={box}><h3 className="text-2xl font-semibold">3 ways to get more bookings</h3><div className="mt-5 divide-y divide-[#ebe5df]">{recommendationItems.map((item) => <div key={item} className="flex items-center justify-between gap-4 py-4"><div><strong className="block font-medium">{item}</strong><p className="mt-1 text-sm text-[#6c625b]">Convert this recommendation into a trackable follow-up task.</p></div><button type="button" onClick={() => { todo(item); note(`Action added: ${item}.`); ping("Action added to your to-dos."); }} className="rounded-full bg-[#f2e9de] px-4 py-2 text-sm font-medium text-[#7c634d]">Apply</button></div>)}</div></section></div>;
  })();

  return (
    <main className="min-h-screen bg-[#fbf9f6] text-[#1f1a17]"><div className="border-b border-[#ebe5df] bg-white"><div className="mx-auto flex max-w-[1280px] items-center justify-between gap-4 px-4 py-4 lg:px-6"><div className="flex items-center gap-6"><button type="button" onClick={() => openNav("Home")} className="grid h-8 w-8 place-items-center rounded-full border border-[#d9d0c8] text-sm font-semibold">A</button><nav className="hidden items-center gap-5 text-sm text-[#4e4641] md:flex">{navs.map((item) => <button key={item} type="button" onClick={() => openNav(item)} className={nav === item ? "font-medium text-[#1f1a17]" : ""}>{item}</button>)}</nav></div><div className="relative flex items-center gap-3"><button type="button" onClick={() => setProfileOpen((current) => !current)} className="grid h-8 w-8 place-items-center rounded-full bg-[#ece7e1] text-xs font-semibold">EJ</button>{profileOpen ? <div className="absolute right-0 top-12 z-20 w-48 rounded-[18px] border border-[#ebe5df] bg-white p-2 shadow-[0_16px_40px_rgba(32,26,23,0.1)]"><button type="button" onClick={() => { setProfileOpen(false); ping("Profile opened."); }} className="w-full rounded-[12px] px-3 py-2 text-left text-sm hover:bg-[#f7f2ed]">Profile</button><button type="button" onClick={() => { setProfileOpen(false); setNav("Performance"); ping("Performance view opened."); }} className="w-full rounded-[12px] px-3 py-2 text-left text-sm hover:bg-[#f7f2ed]">Performance</button><button type="button" onClick={() => { setProfileOpen(false); ping("Settings panel coming soon."); }} className="w-full rounded-[12px] px-3 py-2 text-left text-sm hover:bg-[#f7f2ed]">Settings</button><hr className="my-1 border-[#ebe5df]" /><button type="button" onClick={() => { setProfileOpen(false); handleLogout(); ping("Signed out successfully."); }} className="w-full rounded-[12px] px-3 py-2 text-left text-sm text-[#dc2626] hover:bg-[#fef2f2]">Sign out</button></div> : null}</div></div></div><div className="mx-auto grid max-w-[1280px] gap-10 px-4 py-8 lg:grid-cols-[320px_minmax(0,1fr)] lg:px-6"><aside className="border-b border-[#ebe5df] pb-8 lg:border-b-0 lg:border-r lg:pb-0 lg:pr-8"><div className="flex items-center gap-3"><div className="grid h-10 w-10 place-items-center rounded-full bg-[#efe7dd] text-sm font-semibold">E</div><div><p className="text-sm text-[#8b8179]">Host dashboard</p><h1 className="text-[2rem] font-semibold leading-none">Good afternoon, Emiel</h1></div></div><div className="mt-6 rounded-[20px] bg-[#f6f2ec] p-4"><p className="text-xs uppercase tracking-[0.18em] text-[#8b8179]">Active view</p><strong className="mt-2 block text-lg">{nav}</strong><p className="mt-2 text-sm leading-6 text-[#6c625b]">{nav === "Home" && "All host activity at a glance."}{nav === "Inbox" && "Review and reply to guest conversations."}{nav === "Reservations" && "Track check-ins, checkouts, and upcoming stays."}{nav === "Listings" && "Create and optimize each property page."}{nav === "Performance" && "Watch conversion, rating, and occupancy trends."}</p></div><section className="mt-10"><div className="flex items-center justify-between"><h2 className="text-xl font-semibold">Booking requests</h2><span className="text-sm text-[#8b8179]">{responseRate}% response rate</span></div>{isInboxLoading ? <p className="mt-4 text-sm text-[#6c625b]">Loading guest inquiries...</p> : requests.length === 0 ? <p className="mt-4 text-sm text-[#6c625b]">No guest inquiries yet.</p> : <div className="mt-4 space-y-4">{requests.map((thread) => <article key={thread.id} className="border-b border-[#ebe5df] pb-4"><h3 className="font-medium">{thread.title}</h3><p className="mt-2 text-sm text-[#6c625b]">{thread.stay}</p><p className="text-sm text-[#6c625b]">{thread.room}</p><div className="mt-3 flex items-center gap-3"><button type="button" onClick={() => openThread(thread, `Hi ${thread.guest}, I'll share all the details in a moment.`)} className="text-sm font-medium text-[#0f766e]">{thread.status === "Replied" ? "View reply" : "Respond"}</button><span className={`rounded-full px-2 py-1 text-xs ${thread.status === "Replied" ? "bg-[#dceee4] text-[#2e6b4d]" : "bg-[#f2e9de] text-[#7c634d]"}`}>{thread.status}</span></div></article>)}</div>}</section><section className="mt-10"><div className="flex items-center justify-between"><h2 className="text-xl font-semibold">To-dos</h2>{toDos.length > 0 ? <span className="text-sm text-[#8b8179]">{toDos.length} items</span> : null}</div>{toDos.length === 0 ? <p className="mt-3 text-sm text-[#6c625b]">You&apos;re all caught up!</p> : <div className="mt-4 space-y-3">{toDos.map((task) => <div key={task} className="flex items-start justify-between gap-3 rounded-[16px] bg-[#f6f2ec] px-4 py-3"><p className="text-sm leading-6 text-[#4e4641]">{task}</p><button type="button" onClick={() => { setToDos((current) => current.filter((entry) => entry !== task)); ping("To-do marked complete."); }} className="text-sm font-medium text-[#0f766e]">Done</button></div>)}</div>}</section><section className="mt-10"><h2 className="text-xl font-semibold">Notifications</h2><div className="mt-4 space-y-4">{notifications.map((notification) => <div key={notification.id} className="flex items-start justify-between gap-3 border-b border-[#ebe5df] pb-4"><p className="text-sm leading-6 text-[#6c625b]">{notification.text}</p><button type="button" onClick={() => { setNotifications((current) => current.filter((entry) => entry.id !== notification.id)); ping("Notification dismissed."); }} className="text-sm font-medium text-[#0f766e]">Dismiss</button></div>)}</div></section></aside><section className="min-w-0">{main}</section></div>{toast ? <div className="fixed bottom-5 right-5 z-40 rounded-[18px] bg-[#1f1a17] px-4 py-3 text-sm text-white shadow-[0_18px_40px_rgba(0,0,0,0.22)]">{toast}</div> : null}</main>
  );
}


