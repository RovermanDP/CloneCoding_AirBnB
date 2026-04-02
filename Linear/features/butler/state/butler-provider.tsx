"use client";

import {
  createContext,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { mockProperties } from "@/lib/mock/properties";
import { buildTicketDetail, mockTicketDetails, mockTickets } from "@/lib/mock/tickets";
import { mockTriageRequests } from "@/lib/mock/triage";
import type { Property } from "@/types/property";
import type {
  Ticket,
  TicketActivity,
  TicketChecklistItem,
  TicketDetail,
  TicketStatus,
} from "@/types/ticket";
import type { TriageRequest, TriageRequestStatus } from "@/types/triage";

type ButlerContextValue = {
  properties: Property[];
  tickets: Ticket[];
  triageRequests: TriageRequest[];
  ticketDetails: Record<string, TicketDetail>;
  setTriageRequestStatus: (requestId: string, status: TriageRequestStatus) => void;
  convertRequestToTicket: (requestId: string) => string | null;
  updateTicketStatus: (ticketId: string, status: TicketStatus) => void;
  toggleChecklistItem: (ticketId: string, itemId: string) => void;
  addTicketComment: (ticketId: string, comment: string) => void;
  addTicketNote: (ticketId: string, note: string) => void;
};

const ButlerContext = createContext<ButlerContextValue | null>(null);

function formatNow() {
  return new Intl.DateTimeFormat("ko-KR", {
    month: "numeric",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).format(new Date());
}

function createActivity(actor: string, action: string): TicketActivity {
  return {
    id: crypto.randomUUID(),
    actor,
    action,
    timestamp: formatNow(),
  };
}

function createSeededDetails() {
  const seeded: Record<string, TicketDetail> = {};

  for (const ticket of mockTickets) {
    seeded[ticket.id] = mockTicketDetails[ticket.id] ?? buildTicketDetail(ticket);
  }

  return seeded;
}

function nextTicketId(currentTickets: Ticket[]) {
  const maxId = currentTickets.reduce((acc, ticket) => {
    const parsed = Number(ticket.id.replace("BT-", ""));
    return Number.isNaN(parsed) ? acc : Math.max(acc, parsed);
  }, 0);

  return `BT-${maxId + 1}`;
}

function createTicketFromRequest(request: TriageRequest, ticketId: string): Ticket {
  return {
    id: ticketId,
    title: request.title,
    summary: request.summary,
    propertyName: request.propertyName,
    unitLabel: request.unitLabel,
    tenantName: request.tenantName,
    assignee: "Triage Queue",
    priority: request.suggestedPriority,
    status: request.recommendedStatus,
    requestedAt: "방금 전",
    dueLabel: "오늘 중 분류",
    estimatedCost: request.category === "Document" ? "₩0" : "₩150,000",
    category: request.category,
    tags: [...request.tags, "triage"],
  };
}

function createDetailFromRequest(ticket: Ticket, request: TriageRequest): TicketDetail {
  const detail = buildTicketDetail(ticket);

  return {
    ...detail,
    description: request.summary,
    evidence: [
      `요청 첨부 ${request.attachments}건`,
      `Triage 요청 ${request.id}에서 생성`,
    ],
    checklist: [
      { id: `${ticket.id}-c1`, label: "요청 내용 검토", done: true },
      { id: `${ticket.id}-c2`, label: "담당자 배정", done: false },
      { id: `${ticket.id}-c3`, label: "초기 응답 발송", done: false },
    ],
    activities: [
      createActivity("시스템", `Triage 요청 ${request.id}에서 티켓을 생성`),
      ...detail.activities,
    ],
    internalNotes: ["Triage에서 생성된 신규 티켓입니다.", ...detail.internalNotes],
  };
}

function updateTicketListStatus(tickets: Ticket[], ticketId: string, status: TicketStatus) {
  return tickets.map((ticket) => (ticket.id === ticketId ? { ...ticket, status } : ticket));
}

function updateTicketDetail(
  current: Record<string, TicketDetail>,
  ticketId: string,
  updater: (detail: TicketDetail) => TicketDetail,
) {
  const detail = current[ticketId];
  if (!detail) return current;

  return {
    ...current,
    [ticketId]: updater(detail),
  };
}

export function ButlerProvider({ children }: { children: ReactNode }) {
  const [properties] = useState(mockProperties);
  const [tickets, setTickets] = useState<Ticket[]>(mockTickets);
  const [triageRequests, setTriageRequests] = useState<TriageRequest[]>(mockTriageRequests);
  const [ticketDetails, setTicketDetails] = useState<Record<string, TicketDetail>>(createSeededDetails);

  const value = useMemo<ButlerContextValue>(
    () => ({
      properties,
      tickets,
      triageRequests,
      ticketDetails,
      setTriageRequestStatus(requestId, status) {
        setTriageRequests((current) =>
          current.map((request) => (request.id === requestId ? { ...request, status } : request)),
        );
      },
      convertRequestToTicket(requestId) {
        const request = triageRequests.find((item) => item.id === requestId);
        if (!request || request.status === "Converted") {
          return null;
        }

        const ticketId = nextTicketId(tickets);
        const newTicket = createTicketFromRequest(request, ticketId);
        const detail = createDetailFromRequest(newTicket, request);

        setTickets((current) => [newTicket, ...current]);
        setTicketDetails((current) => ({ ...current, [ticketId]: detail }));
        setTriageRequests((current) =>
          current.map((item) =>
            item.id === requestId ? { ...item, status: "Converted" } : item,
          ),
        );

        return ticketId;
      },
      updateTicketStatus(ticketId, status) {
        setTickets((current) => updateTicketListStatus(current, ticketId, status));
        setTicketDetails((current) =>
          updateTicketDetail(current, ticketId, (detail) => ({
            ...detail,
            status,
            activities: [
              createActivity("관리자", `상태를 ${detail.status}에서 ${status}(으)로 변경`),
              ...detail.activities,
            ],
          })),
        );
      },
      toggleChecklistItem(ticketId, itemId) {
        setTicketDetails((current) =>
          updateTicketDetail(current, ticketId, (detail) => {
            let toggledLabel = "";
            let nextDone = false;

            const checklist = detail.checklist.map((item: TicketChecklistItem) => {
              if (item.id !== itemId) return item;
              toggledLabel = item.label;
              nextDone = !item.done;
              return { ...item, done: !item.done };
            });

            return {
              ...detail,
              checklist,
              activities: toggledLabel
                ? [
                    createActivity(
                      "관리자",
                      `체크리스트 "${toggledLabel}" 항목을 ${nextDone ? "완료" : "미완료"} 처리`,
                    ),
                    ...detail.activities,
                  ]
                : detail.activities,
            };
          }),
        );
      },
      addTicketComment(ticketId, comment) {
        const trimmed = comment.trim();
        if (!trimmed) return;

        setTicketDetails((current) =>
          updateTicketDetail(current, ticketId, (detail) => ({
            ...detail,
            activities: [createActivity("관리자", `댓글 등록: ${trimmed}`), ...detail.activities],
          })),
        );
      },
      addTicketNote(ticketId, note) {
        const trimmed = note.trim();
        if (!trimmed) return;

        setTicketDetails((current) =>
          updateTicketDetail(current, ticketId, (detail) => ({
            ...detail,
            internalNotes: [trimmed, ...detail.internalNotes],
            activities: [createActivity("관리자", "내부 메모를 추가"), ...detail.activities],
          })),
        );
      },
    }),
    [properties, ticketDetails, tickets, triageRequests],
  );

  return <ButlerContext.Provider value={value}>{children}</ButlerContext.Provider>;
}

export function useButlerData() {
  const context = useContext(ButlerContext);

  if (!context) {
    throw new Error("useButlerData must be used within ButlerProvider");
  }

  return context;
}

