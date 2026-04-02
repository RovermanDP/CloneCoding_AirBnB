"use client";

import Link from "next/link";
import { useButlerData } from "@/features/butler/state/butler-provider";

export function ScheduleWorkspace() {
  const { tickets, triageRequests } = useButlerData();

  const todayItems = tickets.filter(
    (ticket) => ticket.dueLabel.includes("오늘") || ticket.dueLabel.includes("금일"),
  );
  const thisWeekItems = tickets.filter(
    (ticket) => ticket.status === "Assigned" || ticket.status === "Working" || ticket.status === "Inspecting",
  );
  const followUpItems = triageRequests.filter((request) => request.status === "On Hold");

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <p className="eyebrow">Repeating ops</p>
          <h1>Schedule</h1>
          <p className="muted">오늘 처리, 이번 주 진행, 보류 후속 조치를 묶어서 보는 운영 일정 화면입니다.</p>
        </div>
      </header>

      <section className="metric-grid">
        <article className="metric-card">
          <span className="muted">오늘 처리</span>
          <strong>{todayItems.length}</strong>
        </article>
        <article className="metric-card">
          <span className="muted">이번 주 진행</span>
          <strong>{thisWeekItems.length}</strong>
        </article>
        <article className="metric-card">
          <span className="muted">보류 후속</span>
          <strong>{followUpItems.length}</strong>
        </article>
      </section>

      <section className="split-grid">
        <article className="placeholder-card">
          <h3>오늘 처리</h3>
          <ul className="placeholder-list">
            {todayItems.map((item) => (
              <li key={item.id}>
                <Link href={`/tasks/${item.id}`}>
                  {item.id} · {item.title} · {item.dueLabel}
                </Link>
              </li>
            ))}
          </ul>
        </article>

        <article className="placeholder-card">
          <h3>이번 주 진행</h3>
          <ul className="placeholder-list">
            {thisWeekItems.map((item) => (
              <li key={item.id}>
                <Link href={`/tasks/${item.id}`}>
                  {item.id} · {item.status} · {item.propertyName}
                </Link>
              </li>
            ))}
          </ul>
        </article>
      </section>

      <section className="placeholder-card">
        <h3>후속 확인 필요</h3>
        {followUpItems.length === 0 ? (
          <div className="empty-state" style={{ marginTop: 16 }}>
            보류된 접수 요청이 없습니다.
          </div>
        ) : (
          <ul className="placeholder-list">
            {followUpItems.map((item) => (
              <li key={item.id}>
                <Link href="/triage">
                  {item.id} · {item.title} · {item.receivedAt}
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
