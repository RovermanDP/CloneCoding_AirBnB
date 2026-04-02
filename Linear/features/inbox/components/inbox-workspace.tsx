"use client";

import Link from "next/link";
import { useMemo, useState } from "react";
import { useButlerData } from "@/features/butler/state/butler-provider";

type InboxFilter = "All" | "Approvals" | "Triage" | "Activity";

type InboxItem = {
  id: string;
  type: InboxFilter | "Alert";
  title: string;
  summary: string;
  href: string;
};

const filters: InboxFilter[] = ["All", "Approvals", "Triage", "Activity"];

export function InboxWorkspace() {
  const { ticketDetails, tickets, triageRequests } = useButlerData();
  const [activeFilter, setActiveFilter] = useState<InboxFilter>("All");

  const items = useMemo<InboxItem[]>(() => {
    const approvals: InboxItem[] = tickets
      .filter((ticket) => ticket.status === "Inspecting" || ticket.priority === "Urgent")
      .map((ticket) => ({
        id: `approval-${ticket.id}`,
        type: "Approvals",
        title: `${ticket.id} 승인/검토 필요`,
        summary: `${ticket.title} · ${ticket.status} · ${ticket.propertyName}`,
        href: `/tasks/${ticket.id}`,
      }));

    const triageItems: InboxItem[] = triageRequests
      .filter((request) => request.status === "New" || request.status === "On Hold")
      .map((request) => ({
        id: `triage-${request.id}`,
        type: "Triage",
        title: `${request.id} 접수 분류 대기`,
        summary: `${request.title} · ${request.propertyName} · ${request.status}`,
        href: "/triage",
      }));

    const activityItems: InboxItem[] = Object.entries(ticketDetails)
      .flatMap(([ticketId, detail]) =>
        detail.activities.slice(0, 1).map((activity) => ({
          id: `activity-${ticketId}-${activity.id}`,
          type: "Activity" as const,
          title: `${ticketId} 최근 활동`,
          summary: `${activity.actor} · ${activity.action}`,
          href: `/tasks/${ticketId}`,
        })),
      )
      .slice(0, 8);

    return [...approvals, ...triageItems, ...activityItems];
  }, [ticketDetails, tickets, triageRequests]);

  const visibleItems =
    activeFilter === "All" ? items : items.filter((item) => item.type === activeFilter);
  const selectedItem = visibleItems[0] ?? null;

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <p className="eyebrow">Notifications</p>
          <h1>Inbox</h1>
          <p className="muted">승인 요청, 접수함 대기, 최근 활동을 한 화면에서 확인하는 운영 인박스입니다.</p>
        </div>
      </header>

      <section className="filter-row" aria-label="Inbox filters">
        <div className="quick-filter-group">
          {filters.map((filter) => (
            <button
              className={`quick-chip${activeFilter === filter ? " active" : ""}`}
              key={filter}
              onClick={() => setActiveFilter(filter)}
              type="button"
            >
              {filter}
            </button>
          ))}
        </div>
        <div className="input-like">알림 수: {visibleItems.length}</div>
        <div className="input-like">선택 필터: {activeFilter}</div>
      </section>

      <div className="detail-layout">
        <section className="detail-main">
          {visibleItems.length === 0 ? (
            <div className="empty-state">현재 필터에 해당하는 알림이 없습니다.</div>
          ) : (
            <div className="stack">
              {visibleItems.map((item) => (
                <Link className="task-card" href={item.href} key={item.id}>
                  <div className="pill-row">
                    <span className="pill status-chip">{item.type}</span>
                  </div>
                  <h3>{item.title}</h3>
                  <p>{item.summary}</p>
                </Link>
              ))}
            </div>
          )}
        </section>

        <aside className="detail-side">
          {selectedItem ? (
            <section className="detail-block">
              <h3>첫 번째 알림 미리보기</h3>
              <p>{selectedItem.title}</p>
              <div className="activity-list" style={{ marginTop: 16 }}>
                <article>{selectedItem.summary}</article>
                <article>관련 화면으로 이동해 바로 상태 변경이나 검토를 이어갈 수 있습니다.</article>
              </div>
            </section>
          ) : (
            <div className="empty-state">미리볼 알림이 없습니다.</div>
          )}
        </aside>
      </div>
    </div>
  );
}

