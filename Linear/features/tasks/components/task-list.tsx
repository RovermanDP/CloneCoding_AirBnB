import Link from "next/link";
import { priorityClassName } from "@/lib/format";
import type { Ticket } from "@/types/ticket";

export function TaskList({ tickets }: { tickets: Ticket[] }) {
  if (tickets.length === 0) {
    return <div className="empty-state">조건에 맞는 작업이 없습니다.</div>;
  }

  return (
    <section className="task-list" aria-label="Ticket list">
      {tickets.map((ticket) => (
        <Link className="task-list-row" href={`/tasks/${ticket.id}`} key={ticket.id}>
          <div>
            <strong>
              {ticket.id} · {ticket.title}
            </strong>
            <p>{ticket.summary}</p>
          </div>
          <div>
            <strong>{ticket.propertyName}</strong>
            <p>
              {ticket.unitLabel} · {ticket.tenantName}
            </p>
          </div>
          <span className={`pill ${priorityClassName(ticket.priority)}`}>{ticket.priority}</span>
          <div>
            <strong>{ticket.assignee}</strong>
            <p>{ticket.status}</p>
          </div>
          <div>
            <strong>{ticket.dueLabel}</strong>
            <p>{ticket.estimatedCost}</p>
          </div>
        </Link>
      ))}
    </section>
  );
}

