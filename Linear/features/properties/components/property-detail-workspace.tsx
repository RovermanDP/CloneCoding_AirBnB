"use client";

import Link from "next/link";
import { useButlerData } from "@/features/butler/state/butler-provider";

export function PropertyDetailWorkspace({ propertyId }: { propertyId: string }) {
  const { properties, tickets } = useButlerData();
  const property = properties.find((item) => item.id === propertyId);

  if (!property) {
    return <div className="empty-state">해당 자산을 찾을 수 없습니다.</div>;
  }

  const relatedTickets = tickets.filter((ticket) => ticket.propertyName === property.name);
  const openTickets = relatedTickets.filter((ticket) => ticket.status !== "Done");
  const completedTickets = relatedTickets.filter((ticket) => ticket.status === "Done");
  const urgentTickets = openTickets.filter((ticket) => ticket.priority === "Urgent");

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <p className="eyebrow">Property profile</p>
          <h1>{property.name}</h1>
          <p className="muted">{property.address}</p>
        </div>
        <div className="stack-row">
          <span className="pill status-chip">Risk {property.riskScore}</span>
          <span className="pill">Units {property.units}</span>
        </div>
      </header>

      <section className="split-grid">
        <article className="placeholder-card">
          <h3>운영 요약</h3>
          <div className="stats-grid" style={{ marginTop: 16 }}>
            <div className="mini-stat">
              <span className="muted">활성 티켓</span>
              <strong>{openTickets.length}</strong>
            </div>
            <div className="mini-stat">
              <span className="muted">완료 이력</span>
              <strong>{completedTickets.length}</strong>
            </div>
            <div className="mini-stat">
              <span className="muted">긴급 이슈</span>
              <strong>{urgentTickets.length}</strong>
            </div>
            <div className="mini-stat">
              <span className="muted">월간 비용</span>
              <strong>{property.monthlySpend}</strong>
            </div>
          </div>
        </article>

        <article className="placeholder-card">
          <h3>하우스 로그</h3>
          <ul className="placeholder-list">
            <li>최근 점검 메모 · 공용부 소음 민원 재발 확인</li>
            <li>보수 이력 · 3월 누수 보수, 2월 도어락 교체</li>
            <li>문서 상태 · 등기/계약 업로드 완료, 관리비 OCR 검토 대기</li>
          </ul>
        </article>
      </section>

      <section className="placeholder-card">
        <div className="link-row">
          <h3>활성 작업</h3>
          <span className="muted">관련 작업 {openTickets.length}건</span>
        </div>
        {openTickets.length === 0 ? (
          <div className="empty-state" style={{ marginTop: 16 }}>
            진행 중인 작업이 없습니다.
          </div>
        ) : (
          <ul className="placeholder-list">
            {openTickets.map((ticket) => (
              <li key={ticket.id}>
                <Link href={`/tasks/${ticket.id}`}>
                  {ticket.id} · {ticket.title} · {ticket.status}
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}

