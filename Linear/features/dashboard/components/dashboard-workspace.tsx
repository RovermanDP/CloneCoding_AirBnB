"use client";

import Link from "next/link";
import { useButlerData } from "@/features/butler/state/butler-provider";

export function DashboardWorkspace() {
  const { properties, tickets, triageRequests } = useButlerData();

  const openTickets = tickets.filter((ticket) => ticket.status !== "Done");
  const urgentTickets = openTickets.filter((ticket) => ticket.priority === "Urgent");
  const activeTriage = triageRequests.filter((request) => request.status === "New" || request.status === "On Hold");
  const reviewQueue = tickets.filter((ticket) => ticket.status === "Inspecting" || ticket.status === "Reviewing");

  const portfolioSnapshot = properties.map((property) => {
    const relatedTickets = openTickets.filter((ticket) => ticket.propertyName === property.name);
    return {
      ...property,
      activeCount: relatedTickets.length,
    };
  });

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <p className="eyebrow">Operations overview</p>
          <h1>Dashboard</h1>
          <p className="muted">Triage, Tasks, Properties에 걸친 운영 현황을 한 번에 보는 메인 대시보드입니다.</p>
        </div>
      </header>

      <section className="metric-grid">
        <article className="metric-card">
          <span className="muted">Open tickets</span>
          <strong>{openTickets.length}</strong>
        </article>
        <article className="metric-card">
          <span className="muted">Urgent issues</span>
          <strong>{urgentTickets.length}</strong>
        </article>
        <article className="metric-card">
          <span className="muted">Triage queue</span>
          <strong>{activeTriage.length}</strong>
        </article>
      </section>

      <section className="placeholder-grid">
        <article className="placeholder-card">
          <h3>Needs action now</h3>
          <p>긴급도와 오늘 마감 기준으로 바로 대응해야 하는 작업입니다.</p>
          <ul className="placeholder-list">
            {openTickets.slice(0, 4).map((ticket) => (
              <li key={ticket.id}>
                <Link href={`/tasks/${ticket.id}`}>{ticket.id} · {ticket.title}</Link>
              </li>
            ))}
          </ul>
        </article>
        <article className="placeholder-card">
          <h3>Triage queue</h3>
          <p>접수함에서 아직 분류가 끝나지 않은 요청입니다.</p>
          <ul className="placeholder-list">
            {activeTriage.map((request) => (
              <li key={request.id}>
                <Link href="/triage">{request.id} · {request.title}</Link>
              </li>
            ))}
          </ul>
        </article>
        <article className="placeholder-card">
          <h3>Review queue</h3>
          <p>검수 또는 검토 단계에 머물러 있는 작업입니다.</p>
          <ul className="placeholder-list">
            {reviewQueue.map((ticket) => (
              <li key={ticket.id}>
                <Link href={`/tasks/${ticket.id}`}>{ticket.id} · {ticket.status}</Link>
              </li>
            ))}
          </ul>
        </article>
      </section>

      <section className="placeholder-grid">
        {portfolioSnapshot.map((property) => (
          <article className="placeholder-card" key={property.id}>
            <h3>{property.name}</h3>
            <p>{property.address}</p>
            <ul className="placeholder-list">
              <li>Active tickets · {property.activeCount}</li>
              <li>Risk score · {property.riskScore}</li>
              <li>Monthly spend · {property.monthlySpend}</li>
            </ul>
          </article>
        ))}
      </section>
    </div>
  );
}

