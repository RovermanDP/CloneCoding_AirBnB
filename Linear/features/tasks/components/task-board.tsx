import Link from "next/link";
import { priorityClassName } from "@/lib/format";
import { ticketColumns } from "@/lib/mock/tickets";
import type { Ticket } from "@/types/ticket";

export function TaskBoard({ tickets }: { tickets: Ticket[] }) {
  return (
    <section className="task-columns" aria-label="Ticket board">
      {ticketColumns.map((status) => {
        const filtered = tickets.filter((ticket) => ticket.status === status);

        return (
          <div className="task-column" key={status}>
            <div className="column-header">
              <h2>{status}</h2>
              <span className="muted">{filtered.length}</span>
            </div>

            {filtered.map((ticket) => (
              <Link className="task-card" key={ticket.id} href={`/tasks/${ticket.id}`}>
                <div className="pill-row">
                  <span className={`pill ${priorityClassName(ticket.priority)}`}>{ticket.priority}</span>
                  <span className="pill status-chip">{ticket.category}</span>
                </div>
                <h3>{ticket.title}</h3>
                <p>{ticket.summary}</p>
                <div className="task-footer">
                  <span>{ticket.propertyName}</span>
                  <span>{ticket.unitLabel}</span>
                </div>
                <div className="task-footer">
                  <span>{ticket.assignee}</span>
                  <span>{ticket.dueLabel}</span>
                </div>
              </Link>
            ))}
          </div>
        );
      })}
    </section>
  );
}

