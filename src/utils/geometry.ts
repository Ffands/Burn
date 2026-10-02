import { BoundingBox, Corner, CornerInfo, OcrRawItem } from '../types/translator';

/**
 * Calculates the corner of a bounding box closest to the screen center (W/2, H/2).
 */
export function calculateNearestCorner(
  box: BoundingBox,
  screenWidth: number,
  screenHeight: number
): CornerInfo {
  const centerX = screenWidth / 2;
  const centerY = screenHeight / 2;

  const corners: Array<{ corner: Corner; x: number; y: number }> = [
    { corner: 'top-left', x: box.x, y: box.y },
    { corner: 'top-right', x: box.x + box.width, y: box.y },
    { corner: 'bottom-left', x: box.x, y: box.y + box.height },
    { corner: 'bottom-right', x: box.x + box.width, y: box.y + box.height },
  ];

  let minDistance = Infinity;
  let nearest = corners[0];

  for (const c of corners) {
    const dx = c.x - centerX;
    const dy = c.y - centerY;
    const dist = Math.sqrt(dx * dx + dy * dy);

    if (dist < minDistance) {
      minDistance = dist;
      nearest = c;
    }
  }

  return {
    corner: nearest.corner,
    x: nearest.x,
    y: nearest.y,
    distanceToCenter: Math.round(minDistance),
  };
}

/**
 * Pure geometric layout clusterer.
 * Merges lines into coherent paragraphs or blocks without relying on punctuation!
 * Preserves columns (tables, multi-column articles, dialog bubbles).
 */
export function clusterTextBlocksGeometric(
  items: OcrRawItem[],
  groupingTightness: number = 1.25
): OcrRawItem[] {
  if (items.length <= 1) return items;

  // Clone items
  const pool = [...items].sort((a, b) => {
    // Sort primarily by Y (top to bottom), secondarily by X (left to right)
    const yDiff = a.box.y - b.box.y;
    if (Math.abs(yDiff) > 8) return yDiff;
    return a.box.x - b.box.x;
  });

  const clusters: Array<{
    items: OcrRawItem[];
    box: BoundingBox;
  }> = [];

  for (const item of pool) {
    let merged = false;

    // Check against existing clusters
    for (const cluster of clusters) {
      const cBox = cluster.box;
      const iBox = item.box;

      // 1. Vertical gap check
      const avgLineHeight = Math.max(12, (cBox.height / Math.max(1, cluster.items.length) + iBox.height) / 2);
      const verticalGap = iBox.y - (cBox.y + cBox.height);
      const maxAllowedGap = avgLineHeight * groupingTightness;

      // If item is below the cluster within allowed line gap
      const isVerticallyAdjacent = verticalGap >= -avgLineHeight * 0.5 && verticalGap <= maxAllowedGap;

      // 2. Horizontal overlap or alignment check (prevents merging distinct table columns)
      const xOverlap = Math.min(cBox.x + cBox.width, iBox.x + iBox.width) - Math.max(cBox.x, iBox.x);
      const isHorizontallyAligned =
        xOverlap > 5 ||
        Math.abs(cBox.x - iBox.x) < 24 ||
        Math.abs((cBox.x + cBox.width) - (iBox.x + iBox.width)) < 24;

      // 3. Column gutter check: if it's strictly to the right or left with significant gap, do not merge!
      const isSeparateColumn =
        (iBox.x > cBox.x + cBox.width + 16) ||
        (cBox.x > iBox.x + iBox.width + 16);

      if (isVerticallyAdjacent && isHorizontallyAligned && !isSeparateColumn) {
        // Merge into this cluster
        cluster.items.push(item);
        const minX = Math.min(cBox.x, iBox.x);
        const minY = Math.min(cBox.y, iBox.y);
        const maxX = Math.max(cBox.x + cBox.width, iBox.x + iBox.width);
        const maxY = Math.max(cBox.y + cBox.height, iBox.y + iBox.height);

        cluster.box = {
          x: minX,
          y: minY,
          width: maxX - minX,
          height: maxY - minY,
        };
        merged = true;
        break;
      }
    }

    if (!merged) {
      clusters.push({
        items: [item],
        box: { ...item.box },
      });
    }
  }

  // Convert clusters back to unified OcrRawItems
  return clusters.map((c, index) => {
    // Sort items within cluster top-to-bottom
    c.items.sort((a, b) => a.box.y - b.box.y);
    const combinedText = c.items.map((i) => i.text.trim()).join(' ');

    return {
      id: `cluster-${index}-${Date.now()}`,
      text: combinedText,
      box: c.box,
      confidence: c.items.reduce((acc, i) => acc + (i.confidence || 0.9), 0) / c.items.length,
    };
  });
}
